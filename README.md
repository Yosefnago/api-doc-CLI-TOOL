# ApiDoc CLI Tool

A lightweight static API documentation generator for Java / Spring Boot applications — with **zero runtime dependencies**.

ApiDoc analyzes your source code using JavaParser (AST), detects controllers and DTOs, and produces clean Markdown documentation without running the application.

---

## ✨ Features

- Static code analysis powered by JavaParser.
- Detects Controllers marked with `@ApiMarker`.
- Detects DTOs marked with `@DtoMarker` (classes or records).
- Extracts endpoints, paths, HTTP methods, parameters, and request/response bodies.
- Generates Markdown documentation files grouped by controller.
- Runs on any machine — no Spring context, no runtime, no server startup.

---

## 📦 Release 1.0.0

Download the latest version:

👉 **https://github.com/Yosefnago/api-doc-CLI-TOOL/releases/tag/v1.0.0**

The release includes:
```
cli/
├─ apidoc ← Linux/Mac executable
├─ apidoc.bat ← Windows executable
└─ apidoc-1.0.0.jar ← Fat JAR (contains all dependencies)
```
---

📦 Installing via Maven Dependency

To use the ApiDoc library in your Java / Spring Boot project, add the following dependency:
```
<dependency>
    <groupId>com.git.apidoc</groupId>
    <artifactId>apidoc</artifactId>
    <version>1.0.2</version>
</dependency>
```
This dependency includes the API scanner, the JavaParser integration, the markers,
and the core logic used to analyze your source code.

⚠️ Note:
The dependency does not automatically expose the CLI command (apidoc).
To use the CLI globally, you must add the cli/ directory to your system PATH
(see instructions below).

⚙️ Adding the CLI to Your System PATH

The release package includes a cli/ folder containing:
```
cli/
├─ apidoc.bat        ← Windows executable
└─ apidoc-1.0.2.jar  ← Fat JAR (all dependencies included)
```
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
🚀 Running the CLI

Once PATH is configured, you can run ApiDoc from anywhere:
```
apidoc generate
apidoc --help
apidoc --version
```
## 📘 Important Notes
```
The Maven dependency gives you Markers, DTO analysis, and API scanning logic.

The CLI (apidoc / apidoc.bat) is provided in the release bundle and must be added to PATH manually.

No Spring context or runtime dependencies are required — the tool performs static AST analysis only.
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

🛠 Requirements

Java 17+

No additional dependencies required

Does not use Spring runtime

🤝 Contributing

Feel free to submit issues .

### 👤 Author

**Yosef Nago**  
Creator and maintainer of the ApiDoc CLI tool.
