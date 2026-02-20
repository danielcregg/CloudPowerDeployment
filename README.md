# CloudPowerDeployment

> **Note:** This repository is a fork of [luxianglin/CloudPowerDeployment](https://github.com/luxianglin/CloudPowerDeployment).

![Java](https://img.shields.io/badge/Java-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![MATLAB](https://img.shields.io/badge/MATLAB-0076A8?style=flat-square&logo=mathworks&logoColor=white)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)

An energy-efficient virtual machine placement strategy for cloud data centers using reinforcement learning. This project implements an optimized Q-Learning(lambda) algorithm and benchmarks it against Q-Learning, Greedy, and PSO algorithms using the CloudSim simulation platform.

## Overview

As cloud data centers grow rapidly, their energy consumption has become a significant concern. This project models the virtual machine (VM) placement problem using reinforcement learning, then optimizes the Q-Learning(lambda) algorithm through state aggregation and temporal credit assignment. Simulation experiments on CloudSim with real-world datasets demonstrate that the optimized algorithm reduces physical server energy consumption more effectively than standard Q-Learning, Greedy, and PSO approaches.

Based on the research paper: "An Energy-Optimized Virtual Machine Placement Strategy Based on Reinforcement Learning."

## Features

- Q-Learning(lambda) algorithm with state aggregation and temporal credit optimizations
- Comparison benchmarks against Q-Learning, Greedy, SARSA, and PSO algorithms
- Power models for real server hardware (HP ProLiant ML110 G5, DL360 G7, DL360 Gen9)
- CloudSim 4.0 integration for realistic cloud simulation
- MATLAB integration for generating experiment result charts
- Multiple test scenarios: algorithm comparison, task scaling, CPU utilization, state convergence, and temporal reliability

## Prerequisites

- **Java** 8 or later
- **Maven** for dependency management
- **MATLAB** (optional, for generating charts)
- **CloudSim 4.0** (included in the `jars/` directory)

## Getting Started

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/danielcregg/CloudPowerDeployment.git
   cd CloudPowerDeployment
   ```

2. Build with Maven:

   ```bash
   mvn compile
   ```

### Usage

Run any of the test scenarios in the `src/main/java/newcloud/Test/` directory:

| Test Class | Description |
|------------|-------------|
| `AlgorithmCompare` | Compares energy consumption across Q-Learning(lambda), Q-Learning, Greedy, and PSO |
| `TaskCompare` | Evaluates performance with varying numbers of VM placement requests |
| `CPUUtilizationCompare` | Analyzes CPU utilization across different algorithms |
| `StateConverge` | Tests state aggregation convergence behavior |
| `TimeReliability` | Evaluates temporal credit assignment reliability |

Example:

```bash
mvn exec:java -Dexec.mainClass="newcloud.Test.AlgorithmCompare"
```

> **Note:** Chart generation requires MATLAB to be installed. Without MATLAB, the algorithms will still run and produce numerical results.

## Tech Stack

- **Language:** Java 8
- **Build Tool:** Maven
- **Simulation:** CloudSim 4.0
- **Optimization:** jswarm-pso (Particle Swarm Optimization)
- **Visualization:** MATLAB (via Java-MATLAB bridge)
- **Data Processing:** Apache POI

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
