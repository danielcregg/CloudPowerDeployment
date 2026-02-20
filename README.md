# CloudPowerDeployment

> Energy-efficient virtual machine placement using reinforcement learning and CloudSim 4.0.

![Java](https://img.shields.io/badge/Java_8-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![CloudSim](https://img.shields.io/badge/CloudSim_4.0-0076A8?style=flat-square)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)

## Overview

This project models the virtual machine (VM) placement problem in cloud data centers as a reinforcement learning (RL) problem. It implements multiple RL algorithms and baseline strategies, then evaluates their energy efficiency using the CloudSim simulation platform with real-world PlanetLab workload traces.

The key insight is that intelligent VM placement can significantly reduce the total energy consumption of a data center by consolidating workloads onto fewer physical servers while maintaining performance SLAs.

> Based on the research paper: *"An Energy-Optimized Virtual Machine Placement Strategy Based on Reinforcement Learning."*
> Fork of [luxianglin/CloudPowerDeployment](https://github.com/luxianglin/CloudPowerDeployment).

## Algorithms Implemented

| Algorithm | Type | Description |
|-----------|------|-------------|
| **Q-Learning** | RL (off-policy) | Standard tabular Q-Learning with epsilon-greedy exploration |
| **Q-Learning(Lambda)** | RL (off-policy) | Q-Learning with eligibility traces for temporal credit assignment |
| **SARSA** | RL (on-policy) | On-policy TD control using actual next-action values |
| **SARSA(Lambda)** | RL (on-policy) | SARSA with eligibility traces |
| **Q-Learning + Init** | RL (off-policy) | Q-Learning with reward-based Q-table initialization |
| **Greedy** | Baseline | Places VM on host with minimum energy increase |
| **Fair** | Baseline | Places VM on host with most available MIPS |
| **Random** | Baseline | Random host selection (lower bound) |

## State Space, Action Space, and Reward Function

### State Space (Feature-Based)
Instead of the intractable per-host utilization encoding, the state is represented by three aggregate features:
- **Average CPU utilization** across all hosts (discretized into 10 bins)
- **Active host ratio** - fraction of hosts running at least one VM (10 bins)
- **VM density** - total VMs divided by 10, capped at 9

This gives a state space of ~1,000 states, compared to 10^300 in the original design.

### Action Space
The action is selecting a physical host (0 to N-1) for placing the incoming VM.

### Reward Function
The reward is the **negative normalized power consumption**:
```
reward = -currentPower / maxObservedPower
```
This is bounded in [-1, 0], where 0 means zero power consumption (ideal) and -1 means maximum observed power. This replaces the original `Math.pow(lastPower/currentPower, 10000)` which caused numerical overflow/underflow.

## Server Power Models

Three real server hardware models from SPECpower benchmarks:
- **HP ProLiant ML110 G5** (93.7W idle, 135W peak)
- **HP ProLiant DL360 G7** (54.6W idle, 178W peak)
- **HP ProLiant DL360 Gen9** (45W idle, 276W peak)

## Prerequisites

- **Java** 8
- **Maven** 3.x

## Build & Run

```bash
# Clone
git clone https://github.com/danielcregg/CloudPowerDeployment.git
cd CloudPowerDeployment

# Build
mvn compile

# Run algorithm comparison
mvn exec:java -Dexec.mainClass="newcloud.Test.AlgorithmCompare"

# Run with custom workload folder
mvn exec:java -Dexec.mainClass="newcloud.Test.AlgorithmCompare" \
  -Dcloudsim.input.folder="src/main/resources/datas/100"

# Run tests
mvn test
```

## Test Scenarios

| Test Class | Description |
|------------|-------------|
| `newcloud.Test.AlgorithmCompare` | Compares energy consumption across Q-Learning(Lambda), Q-Learning, Greedy, and Q-Learning+Init |
| `newcloud.Test.TaskCompare` | Evaluates performance with varying numbers of VM placement requests (50-300) |
| `newcloud.Test.CPUUtilizationCompare` | Analyzes CPU utilization distribution across different algorithms |
| `newcloud.Test.StateConverge` | Tests state aggregation convergence vs. non-aggregated state |
| `newcloud.Test.TimeReliability` | Evaluates temporal credit assignment (Lambda traces vs. standard) |

## PlanetLab Workload Traces

The `src/main/resources/` directory contains real-world CPU utilization traces from the [PlanetLab](https://www.planet-lab.org/) distributed testbed, collected in March-April 2011. Each file represents one VM's CPU utilization over a 24-hour period, sampled every 5 minutes (288 data points per file).

Available dataset sizes: 50, 100, 150, 200, 250, 300 VMs.

## Project Structure

```
src/main/java/newcloud/
  ├── Constants.java              # Simulation parameters
  ├── datacenter/
  │   ├── PowerDatacenterRL.java  # Base class (shared logic)
  │   ├── PowerDatacenterLearning.java       # Q-Learning
  │   ├── PowerDatacenterLearningLamda.java  # Q-Learning(Lambda)
  │   ├── PowerDatacenterSarsa.java          # SARSA
  │   ├── PowerDatacenterSarsa_lamda.java    # SARSA(Lambda)
  │   ├── PowerDatacenterGready.java         # Greedy
  │   ├── PowerDatacenterFair.java           # Fair
  │   └── PowerDatacenterRandom.java         # Random
  ├── policy/                     # VM allocation strategies
  ├── executedata/                # Experiment execution harnesses
  ├── PowerModel/                 # Server power models (SPECpower)
  └── Test/                       # Comparison experiments
src/test/java/newcloud/           # JUnit tests
jars/                             # CloudSim 4.0 and dependencies
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
