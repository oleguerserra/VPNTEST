# VPN Test Scripts

This project contains two Python scripts (`vpntest-server.py` and `vpntest-man-in-the-middle.py`) for testing VPN connections. These scripts import various libraries and generate logs in CSV format.

## Installation

### Python and pip

Before running the scripts, ensure that Python and pip are installed on your system. Follow the instructions below based on your operating system:

#### Windows

1. Download the latest Python installer from the official website: [Python Downloads](https://www.python.org/downloads/windows/).

2. Run the installer and select the option to install Python for all users. Make sure to check the box that adds Python to the system PATH.

3. Complete the installation by following the on-screen instructions.

#### Linux

Python is usually pre-installed on most Linux distributions. To check if Python is installed, open a terminal and run the following command:

```bash
python --version
```

If Python is not installed, use the package manager specific to your distribution to install Python. For example, on Ubuntu or Debian, you can use the following command:

```bash
sudo apt-get update
sudo apt-get install python3 python3-venv
```

macOS
Python is pre-installed on macOS. To check if Python is installed, open a terminal and run the following command:

```bash
python --version
```

If Python is not installed or you want to install a different version, you can use package managers like Homebrew or pyenv.

## Environment Setup

1. Clone or download this repository to your local machine.

2. Open a terminal or command prompt and navigate to the project directory.

3. Create a Python virtual environment by running the following command:

* For Windows

```bash
python -m venv venv
venv\Scripts\activate
```

* For macOS/Linux

```bash
python3 -m venv venv
source venv/bin/activate
```

4. Install the project dependencies by running the following command:

```bash
pip install -r requirements.txt
```

This will install the required libraries into the virtual environment.

## Running the Scripts

### vpntest-server.py

To run the vpntest-server.py script, use the following command:

```bash
python scripts/vpntest-server.py [start|stop]
```

Replace [start|stop] with one of the following options:

* start to start the server
* stop to stop the server

The script will generate logs in the logs directory.

### vpntest-man-in-the-middle.py

To run the vpntest-man-in-the-middle.py script, use the following command:

```bash
python scripts/vpntest-man-in-the-middle.py <interface> <api_key>
```

Replace <interface> with the Wi-Fi interface of your computer and <api_key> with your API key obtained from ipgeolocation.io.

The script will generate logs in the logs directory.

## License
This project is licensed under the MIT License.

^


