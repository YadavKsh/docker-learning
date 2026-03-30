# 🐳 Dockerfile — Line by Line Explanation

> 📁 These are my personal notes on building Docker images using a `Dockerfile`.  
> Each instruction tells Docker **what to do, step by step**, when building an image.

---

## 📑 Table of Contents
1. [What is a Dockerfile?](#what-is-a-dockerfile)
2. [Dockerfile 1 — Ubuntu Image](#dockerfile-1--ubuntu-image)
3. [Dockerfile 2 — Java App Image](#dockerfile-2--java-app-image)
4. [Quick Reference — Common Dockerfile Instructions](#quick-reference--common-dockerfile-instructions)

---

## What is a Dockerfile?

A `Dockerfile` is a plain text file that contains a set of **instructions** Docker reads top to bottom to **build an image** automatically.

```
Dockerfile  ──►  docker build  ──►  Image  ──►  docker run  ──►  Container
```

Think of it like a **recipe** — each line is a step, and the final result is a ready-to-use Docker image.

---

## Dockerfile 1 — Ubuntu Image

### 📄 The Dockerfile

```dockerfile
FROM ubuntu:latest
LABEL authors="kshit"

RUN apt update

CMD ["echo", "This is my first Ubuntu Image"]
```

---

### 🔍 Line-by-Line Breakdown

---

#### `FROM ubuntu:latest`

```dockerfile
FROM ubuntu:latest
```

- **`FROM`** is always the **first instruction** in every Dockerfile — it is mandatory.
- It sets the **base image** that your image will be built on top of.
- `ubuntu:latest` means Docker will pull the most recent stable version of the official Ubuntu image from Docker Hub.
- Think of this as: *"Start with a fresh Ubuntu system, and I'll customise it from here."*

> 💡 Every image is built on top of another image. Even `ubuntu:latest` itself is built on top of a minimal Linux layer. This is called **layered architecture**.

---

#### `LABEL authors="kshit"`

```dockerfile
LABEL authors="kshit"
```

- **`LABEL`** adds **metadata** (key-value pairs) to the image.
- This is purely informational — it doesn't affect how the image runs.
- Useful for documenting who built the image, the version, description, etc.
- You can add multiple labels:
  ```dockerfile
  LABEL authors="kshit"
  LABEL version="1.0"
  LABEL description="My first Ubuntu Docker image"
  ```

> 💡 Labels can be viewed later using `docker inspect <image_name>`.

---

#### `RUN apt update`

```dockerfile
RUN apt update
```

- **`RUN`** executes a **shell command inside the container during the build process**.
- `apt update` refreshes Ubuntu's package list so you can install packages with `apt install`.
- This creates a **new layer** in the image with the result of the command baked in.
- `RUN` is used for anything you want to set up *at build time* — installing packages, creating directories, setting permissions, etc.

> ⚠️ `RUN` runs at **build time** (when you do `docker build`).  
> It is NOT the command that runs when you start a container — that's `CMD`.

---

#### `CMD ["echo", "This is my first Ubuntu Image"]`

```dockerfile
CMD ["echo", "This is my first Ubuntu Image"]
```

- **`CMD`** defines the **default command** that runs when the container starts (i.e., when you do `docker run`).
- This uses the **exec form** (JSON array) — preferred over the shell form because it doesn't spawn an extra shell process.
- Here it runs the `echo` command, which simply prints the message to the terminal.
- There can only be **one `CMD`** in a Dockerfile — if you write multiple, only the last one is used.
- `CMD` can be **overridden** at runtime:
  ```bash
  docker run my-ubuntu-image echo "Override message"
  ```

> 💡 **`RUN` vs `CMD`:**
> | | `RUN` | `CMD` |
> |---|---|---|
> | When it runs | At **build time** | At **container start** |
> | Purpose | Set up the image | Default startup command |
> | Creates a layer? | ✅ Yes | ❌ No |

---

### 🧅 What the Final Image Looks Like (Layers)

```
[ CMD — echo message          ]  ← default startup command
           │
[ RUN — apt update            ]  ← new layer: updated package list
           │
[ LABEL — authors="kshit"     ]  ← metadata only, no new layer
           │
[ FROM — ubuntu:latest        ]  ← base layer: Ubuntu OS
```

---

### ▶️ Build and Run

```bash
# Build the image
docker build -t my-ubuntu-image:1.0 .

# Run the container
docker run my-ubuntu-image:1.0
# Output: This is my first Ubuntu Image
```

---
---

## Dockerfile 2 — Java App Image

### 📄 The Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
LABEL authors="kshit"

WORKDIR /usr/src/myapp

COPY . /usr/src/myapp

RUN javac src/Test.java

CMD ["java", "-cp", "src", "Test"]
```

---

### 🔍 Line-by-Line Breakdown

---

#### `FROM eclipse-temurin:21-jdk-jammy`

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
```

- Sets the **base image** to Eclipse Temurin's JDK 21 build on Ubuntu Jammy (22.04).
- `eclipse-temurin` is the **official, recommended Java base image** on Docker Hub (the older `openjdk` image is deprecated).
- `21` → Java version 21 (LTS).
- `jdk` → Full Java Development Kit, which includes `javac` (the compiler). Required here because we need to **compile** the Java source file.
- `jammy` → Built on top of Ubuntu 22.04 LTS ("Jammy Jellyfish").

> ⚠️ If you used `jre` instead of `jdk`, the build would fail with `javac: not found` because the JRE only includes the runtime — not the compiler.

---

#### `LABEL authors="kshit"`

```dockerfile
LABEL authors="kshit"
```

- Same as before — adds **metadata** to the image. Purely informational.
- No impact on how the image or container behaves.

---

#### `WORKDIR /usr/src/myapp`

```dockerfile
WORKDIR /usr/src/myapp
```

- **`WORKDIR`** sets the **working directory** inside the container for all subsequent instructions (`COPY`, `RUN`, `CMD`, etc.).
- If the directory doesn't exist, Docker **creates it automatically**.
- Think of it as doing `mkdir -p /usr/src/myapp && cd /usr/src/myapp` — but the cleaner, Docker-native way.
- All relative paths in later instructions are resolved from this directory.

> 💡 Always use `WORKDIR` instead of `RUN cd /some/path` — it's cleaner, more readable, and persists across instructions.

---

#### `COPY . /usr/src/myapp`

```dockerfile
COPY . /usr/src/myapp
```

- **`COPY`** copies files/directories from your **host machine** (build context) into the **image**.
- `.` → source: everything in the current directory on your host (where you run `docker build`).
- `/usr/src/myapp` → destination: inside the container (same as `WORKDIR`).
- This is how your Java source files (`src/Test.java`, etc.) get into the image.

> 💡 **`COPY` vs `ADD`:**  
> Prefer `COPY` for straightforward file copying. `ADD` has extra features (like auto-extracting `.tar` files and fetching from URLs) but those are rarely needed and can cause unexpected behaviour.

---

#### `RUN javac src/Test.java`

```dockerfile
RUN javac src/Test.java
```

- Runs the **Java compiler** (`javac`) during the image build.
- Compiles `src/Test.java` → produces `src/Test.class`.
- This happens **at build time**, so the compiled `.class` file is baked directly into the image.
- Since `WORKDIR` is `/usr/src/myapp`, the full path being compiled is `/usr/src/myapp/src/Test.java`.

> ⚠️ This only works because we used the `jdk` variant (which includes `javac`). With `jre`, this line would fail.

---

#### `CMD ["java", "-cp", "src", "Test"]`

```dockerfile
CMD ["java", "-cp", "src", "Test"]
```

- Defines the **default command** to run when the container starts.
- `java` → the Java runtime (launches the JVM).
- `-cp src` → sets the **classpath** to the `src/` directory, telling Java where to look for `.class` files.
- `Test` → the name of the class to run (Java will look for `Test.class` in the classpath).
- Without `-cp src`, Java wouldn't know where to find `Test.class` and the container would fail with `ClassNotFoundException`.

> 💡 In real Spring Boot projects, you'd replace this with:
> ```dockerfile
> CMD ["java", "-jar", "app.jar"]
> ```
> because everything is packaged into a single `.jar` file.

---

### 🧅 What the Final Image Looks Like (Layers)

```
[ CMD — java -cp src Test         ]  ← default startup: run Test.class
              │
[ RUN — javac src/Test.java       ]  ← new layer: Test.class compiled into image
              │
[ COPY — . → /usr/src/myapp       ]  ← new layer: your source files copied in
              │
[ WORKDIR — /usr/src/myapp        ]  ← sets working directory
              │
[ LABEL — authors="kshit"         ]  ← metadata only
              │
[ FROM — eclipse-temurin:21-jdk   ]  ← base: Ubuntu Jammy + JDK 21
```

---

### ▶️ Build and Run

```bash
# Build the image
docker build -t my-java-app:1.0 .

# Run the container
docker run my-java-app:1.0
# Output: whatever Test.java prints
```

---
---

## Quick Reference — Common Dockerfile Instructions

| Instruction | Purpose | When it runs |
|---|---|---|
| `FROM` | Sets the base image | Build time (first step) |
| `LABEL` | Adds metadata (key=value) | Build time (no layer) |
| `WORKDIR` | Sets working directory inside container | Build time |
| `COPY` | Copies files from host → image | Build time |
| `RUN` | Executes a shell command | Build time (creates a layer) |
| `CMD` | Default command when container starts | Run time |
| `ENTRYPOINT` | Like CMD but harder to override | Run time |
| `ENV` | Sets environment variables | Build + Run time |
| `EXPOSE` | Documents which port the app uses | Informational only |
| `ARG` | Build-time variable (not available at runtime) | Build time only |

> 💡 **Golden Rule:**
> - `RUN` = "do this while **building** the image"
> - `CMD` = "do this when **starting** the container"