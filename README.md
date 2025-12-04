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

## 🚀 Usage

### **Windows**
```bat
apidoc.bat generate
```
Linux / macOS
```
./apidoc generate
```
Using the JAR directly (all platforms)
```
java -jar apidoc-1.0.0.jar generate
```
```
my-spring-project/
  ├─ src/
  │   └─ main/java/...  ← Controllers, DTOs, etc.
  └─ ...

apidoc-cli/
  └─ apidoc / apidoc.bat / apidoc-1.0.0.jar

# Execute:
cd my-spring-project
../apidoc-cli/apidoc generate
```
```
This generates:
api-docs/
  ├─ UsersController.md
  ├─ OrdersController.md
  └─ ...
```

## 🧪 Markers
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
