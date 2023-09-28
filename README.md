# basic-oai-harvester

This is a simple command-line tool for harvesting metadata using the OAI-PMH protocol. It provides three main functions:

1. **Start a New Harvest:** Initiate a new metadata harvest from an OAI-PMH data source.
2. **Resume an Interrupted Harvest:** Continue a harvest that was interrupted.
3. **Update a Completed Harvest:** Update a previously completed harvest to retrieve any new or changed metadata.

## Table of Contents

- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)
- [Contact](#contact)

## Installation

To get tarted:

1. Download the zip file corresponding to your operating system from the GitHub repository release page (`ubuntu` should
   work on most Linux distributions).
2. Unzip the file and place the `basic-oai-harvester` directory in a convenient location.

## Usage

For general information about the program, run (use `basic-oai-harvester.exe` instead on Windows):

```bash
basic-oai-harvester
```

To get help with a specific command, run:

```bash
basic-oai-harvester help harvest
# or
basic-oai-harvester help resume
# or
basic-oai-harvester help update
```

The three main commands are:

```bash
# Start a new harvest
basic-oai-harvester harvest [OAI-PMH endpoint] [options]

# Resume an interrupted harvest
basic-oai-harvester resume [options]

# Update a completed harvest
basic-oai-harvester update [options]
```

For example, to start a new complete harvest from the OAI-PMH endpoint `https://oai.example.org/oai` and save the
results to a directory named `results` under the current directory, run:

```bash
basic-oai-harvester harvest https://oai.example.org/oai -d results
```

## Contributing

If you have suggestions for improvements or would like to report a bug, please create an issue.

If you would like to contribute a feature or improvements, please start by looking again at the name of the repository,
then if you still think it makes sense, please create an issue to discuss it first.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

This project is written in Java, uses Maven, and relies on various third-party libraries and dependencies. Check out
the [pom.xml](pom.xml) file for a list of these dependencies.

## Contact

For questions, bug reports, or contributions, please create an issue.
