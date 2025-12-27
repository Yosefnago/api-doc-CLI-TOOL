# ApiDoc CLI Tool

A lightweight static API documentation generator for Java / Spring Boot applications with **zero runtime dependencies**.

ApiDoc analyzes your source code using JavaParser (AST), detects controllers and DTOs, and produces clean Markdown documentation without running the application.


## 📚 Table of Contents

- [Features](#-features)
- [Release 1.1.0](#-release-110)
- [Installing via Maven Dependency](#-installing-via-maven-dependency)
- [Adding the CLI to Your System PATH](#-adding-the-cli-to-your-system-path)
- [Running the CLI](#-running-the-cli)
- [Important Notes](#-important-notes)
- [Dont forget to mark your classes before running](#-dont-forget-to-mark-your-classes-before-running)
- [Requirements](#-requirements)
- [Contributing](#-contributing)
- [Author](#-author)
- [License](#license)

---

## ✨ Features

- Static code analysis powered by JavaParser.
- Detects Controllers marked with `@ApiMarker`.
- Detects DTOs marked with `@DtoMarker` (classes or records).
- Extracts endpoints, paths, HTTP methods, parameters, and request/response bodies.
- Generates Markdown documentation files grouped by controller.
- Runs on any machine — no Spring context, no runtime, no server startup required.
- Supports multi-module Maven projects (microservices + shared libraries).

---

## 📦 Release 1.1.0

Download the latest version:

👉 **https://github.com/Yosefnago/api-doc-CLI-TOOL/releases/tag/v1.1.0**

The release includes:
```
cli/
├─ apidoc.bat           ← Windows launcher script
├─ apidoc-1.1.0.jar     ← Fat JAR (ApiDoc CLI + all dependencies)
├─ config.properties   ← Stores the current project root path
└─ logging.properties  ← Logging configuration for the CLI
```
---

## 📦 Installing via Maven Dependency

To use the ApiDoc library in your Java / Spring Boot project, add the following dependency:
```
<dependency>
    <groupId>com.git.apidoc</groupId>
    <artifactId>apidoc</artifactId>
    <version>1.1.0</version>
</dependency>
```
This dependency includes the API scanner, the JavaParser integration, the markers,
and the core logic used to analyze your source code.

⚠️ Note:
The dependency does not automatically expose the CLI command (apidoc).
To use the CLI globally, you must add the cli/ directory to your system PATH
(see instructions below).

## ⚙️ Adding the CLI to Your System PATH

To run apidoc as a system-wide command, add this folder to your OS PATH.

🪟 Windows

1. Open
Start → Edit the system environment variables

2. Click Environment Variables

3. Under System variables, edit the variable:
Path

4. Add a new entry:
```
C:\path\to\apidoc\cli\
```
5. Click OK → OK → OK

6. Sign out and sign back in (required for Windows to refresh PATH).

Verify:
```
where apidoc
```
You should see:
```
C:\path\to\apidoc\cli\apidoc.bat
```
## 🚀 Running the CLI

Once PATH is configured, you can run ApiDoc from anywhere:
```
apidoc np <path>    # Set the project or workspace root
apidoc status       # Show the currently configured root path
apidoc generate     # Generate API documentation for the configured root
apidoc --help       # Display available commands
apidoc --version    # Display the installed ApiDoc version
```
## 📘 Important Notes
```
The project root is stored in `cli/config.properties`.

This allows the CLI to be portable and consistent across different projects.

Multi-module Maven projects (microservices with shared modules) are fully supported.

ApiDoc performs static analysis only — no Spring context or runtime execution is required.
```
## ⚠️ Dont forget to mark your classes before running!

Controller marker:
```
@ApiMarker
public class UsersController { ... }
```
DTO marker:
```
@DtoMarker
public class/record UserDto { ... }
```

## 🛠 Requirements

Java 17+

No additional dependencies required

Does not use Spring runtime

## 🤝 Contributing

Feel free to submit issues .

### 👤 Author

**Yosef Nago**  
Creator and maintainer of the ApiDoc CLI tool.


## License

See the full license here: [LICENSE](./LICENSE)
