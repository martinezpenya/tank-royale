# Python Bot API for Robocode Tank Royale

Note: This README is for developers—not PyPi. The README for PyPi is [here](README-PyPI.md)

This directory contains the Bot API for developing bots for Robocode Tank Royale with Python.

The Bot API is provided via a pip package.

## Build Commands (Local Build)

### From the current directory:

First, generate schemas into a tank_royale.schema package:

```shell
python scripts/schema_to_python.py -d ../../schema/schemas -o generated/robocode_tank_royale/schema
```

Then install a local package using in "editable" mode setup.py, linking the installed package to the source directory
for live updates:

```shell
pip install -e .
```

### From the root folder using Gradle:

#### Clean:

```shell
../../gradlew :bot-api:python:clean --info
```

#### Build:

```shell
../../gradlew :bot-api:python:build --info
```

The artifacts are located in `/bot-api/python/dist`.

#### Test:

```shell
../../gradlew :bot-api:python:test --info
```

## Usage

```py
import robocode_tank_royale.bot_api
```
