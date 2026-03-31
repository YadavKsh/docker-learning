# 🐳 Dockerfile — Line by Line Explanation

> 📁 These are my personal notes on building Docker images using a `Dockerfile`.  
> Each instruction tells Docker **what to do, step by step**, when building an image.

---

## 📑 Table of Contents
1. [What is a Dockerfile?](#what-is-a-dockerfile)
2. [Dockerfile 1 — Ubuntu Image](#dockerfile-1--ubuntu-image)
3. [Dockerfile 2 — Java App Image](#dockerfile-2--java-app-image)
4. [Dockerfile 3 — Python App Image](#dockerfile-3--python-app-image)
5. [Dockerfile 4 — Spring Boot App Image](#dockerfile-4--spring-boot-app-image)
6. [🔨 docker build — Explained](#-docker-build--explained)
7. [Quick Reference — Common Dockerfile Instructions](#quick-reference--common-dockerfile-instructions)

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

## Dockerfile 3 — Python App Image

### 📄 The Dockerfile

```dockerfile
FROM python:latest
LABEL authors="kshit"

WORKDIR /usr/src/myapp
COPY . /usr/src/myapp

CMD ["python3", "main.py"]
```

---

### 🔍 Line-by-Line Breakdown

---

#### `FROM python:latest`

```dockerfile
FROM python:latest
```

- Sets the **base image** to the official Python image from Docker Hub.
- `python:latest` pulls the most recent stable version of Python available.
- This image comes with Python, `pip`, and all standard libraries pre-installed — you don't need to install Python yourself.
- The base OS underneath is **Debian Linux** (unless you pick a variant like `alpine`).

> ⚠️ **Best practice:** Avoid `latest` in real projects. Pin to a specific version like `python:3.12` so your build doesn't unexpectedly break when a new Python version is released.
>
> | Tag | What you get |
> |---|---|
> | `python:latest` | Newest version (unpredictable) |
> | `python:3.12` | Python 3.12 specifically |
> | `python:3.12-slim` | Python 3.12 on a minimal Debian (smaller image) |
> | `python:3.12-alpine` | Python 3.12 on Alpine Linux (smallest, ~5MB base) |

---

#### `LABEL authors="kshit"`

```dockerfile
LABEL authors="kshit"
```

- Adds **metadata** to the image — purely informational, no effect on behaviour.
- Viewable later via `docker inspect pythonimage`.

---

#### `WORKDIR /usr/src/myapp`

```dockerfile
WORKDIR /usr/src/myapp
```

- Sets `/usr/src/myapp` as the **working directory** inside the container.
- All subsequent `COPY`, `RUN`, and `CMD` instructions operate relative to this path.
- Docker creates this directory automatically if it doesn't exist.

---

#### `COPY . /usr/src/myapp`

```dockerfile
COPY . /usr/src/myapp
```

- Copies **everything** from your current host directory (`.`) into `/usr/src/myapp` inside the image.
- This includes your `main.py` and any other project files (e.g., `requirements.txt`, helper modules, etc.).
- Since `WORKDIR` is already `/usr/src/myapp`, you could also write this as just `COPY . .` — the destination `.` would resolve to the `WORKDIR`.

> 💡 **Tip — install dependencies before copying source code:**  
> If your project has a `requirements.txt`, it's better practice to copy and install it *before* copying the rest of the code. This way Docker can cache the dependency layer and won't reinstall packages on every rebuild unless `requirements.txt` actually changes:
> ```dockerfile
> COPY requirements.txt .
> RUN pip install -r requirements.txt
> COPY . .
> ```

---

#### `CMD ["python3", "main.py"]`

```dockerfile
CMD ["python3", "main.py"]
```

- Defines the **default command** to execute when the container starts.
- `python3` → the Python 3 interpreter.
- `main.py` → your Python script to run (must exist in `WORKDIR` after the `COPY`).
- Uses **exec form** (JSON array) — preferred because it runs `python3` directly without wrapping it in a shell.

> 💡 **Shell form vs Exec form:**
> ```dockerfile
> CMD python3 main.py           # shell form — runs via /bin/sh -c
> CMD ["python3", "main.py"]    # exec form  — runs directly ✅ preferred
> ```
> The exec form is preferred because signals like `SIGTERM` (from `docker stop`) are sent directly to `python3`, allowing clean shutdown.

---

### 🧅 What the Final Image Looks Like (Layers)

```
[ CMD — python3 main.py        ]  ← default startup: run main.py
              │
[ COPY — . → /usr/src/myapp    ]  ← new layer: your project files copied in
              │
[ WORKDIR — /usr/src/myapp     ]  ← sets working directory
              │
[ LABEL — authors="kshit"      ]  ← metadata only, no new layer
              │
[ FROM — python:latest         ]  ← base: Debian Linux + Python + pip
```

> 💡 Notice there's no `RUN` step here — unlike the Java image, Python is an **interpreted language**. There's no compilation step needed. Docker copies your `.py` file in and Python runs it directly at container start.

---

### ▶️ Build and Run

```bash
# Build the image
docker build -t pythonimage .

# Run the container
docker run pythonimage
# Output: whatever main.py prints
```

---
---

## Dockerfile 4 — Spring Boot App Image

### 📄 Project Structure

This is a real **Spring Boot REST API** project dockerized. Here's what the project contains:

**`TestController.java`** — a REST controller that exposes a single GET endpoint at `/`:
```java
package com.docker.test.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    @GetMapping("/")
    public Map<String, Object> getValues() {
        Map<String, Object> map = new HashMap<>();
        map.put("message", "Java API is working fine!");
        map.put("languages", Arrays.asList("Java", "Python", "JavaScript"));
        map.put("code", 2345);
        return map;
    }
}
```

**`DockerTestApplication.java`** — the main Spring Boot entry point:
```java
package com.docker.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DockerTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(DockerTestApplication.class, args);
    }
}
```

**`Dockerfile`**:
```dockerfile
FROM eclipse-temurin:21-jdk-jammy
LABEL authors="kshit"

WORKDIR /usr/src/myapp
COPY . /usr/src/myapp

CMD ["java", "-jar", "DockerTest-0.0.1-SNAPSHOT.jar"]
EXPOSE 9595
```

---

### 🔍 Line-by-Line Breakdown

---

#### `FROM eclipse-temurin:21-jdk-jammy`

```dockerfile
FROM eclipse-temurin:21-jdk-jammy
```

- Same base image as Dockerfile 2 — Eclipse Temurin JDK 21 on Ubuntu Jammy.
- Uses `jdk` here because Maven/Gradle (used to build the `.jar`) needs the full JDK.
- In production, you'd ideally switch this to `jre` (smaller image) since you only need to *run* the pre-built `.jar`, not compile anything.

> 💡 **Production tip — use JRE for running, JDK only for building:**
> ```dockerfile
> FROM eclipse-temurin:21-jre-jammy   # smaller, for running the .jar only
> ```
> Even better — use a **multi-stage build** (build with JDK, run with JRE). More on this below.

---

#### `LABEL authors="kshit"`

```dockerfile
LABEL authors="kshit"
```

- Metadata only. No effect on the running container.

---

#### `WORKDIR /usr/src/myapp`

```dockerfile
WORKDIR /usr/src/myapp
```

- Sets `/usr/src/myapp` as the working directory inside the container.
- All subsequent instructions (`COPY`, `CMD`) resolve paths relative to this directory.

---

#### `COPY . /usr/src/myapp`

```dockerfile
COPY . /usr/src/myapp
```

- Copies **everything** from your project directory on the host into `/usr/src/myapp` inside the image.
- This includes your pre-built JAR file (`DockerTest-0.0.1-SNAPSHOT.jar`), which is assumed to already exist in your project folder (built by Maven/Gradle before `docker build`).

> ⚠️ **Important — build your JAR before running `docker build`:**  
> Docker doesn't run Maven or Gradle for you automatically here. You must build the JAR first:
> ```bash
> ./mvnw clean package -DskipTests    # Maven
> # or
> ./gradlew bootJar                    # Gradle
>
> # Then build the Docker image
> docker build -t springimage .
> ```
> The JAR file (`target/DockerTest-0.0.1-SNAPSHOT.jar`) must exist before the `COPY` instruction can pick it up.

---

#### `CMD ["java", "-jar", "DockerTest-0.0.1-SNAPSHOT.jar"]`

```dockerfile
CMD ["java", "-jar", "DockerTest-0.0.1-SNAPSHOT.jar"]
```

- Defines the **default command** to run when the container starts.
- `java` → launches the JVM.
- `-jar` → tells Java to run a self-contained executable JAR file.
- `DockerTest-0.0.1-SNAPSHOT.jar` → the Spring Boot fat JAR (contains your app + all dependencies + embedded Tomcat).
- Spring Boot packages everything into one JAR — there's no separate server to set up. Just run the JAR and the app starts on its configured port (here: `9595`).

> 💡 **What is a fat JAR?**  
> A Spring Boot fat JAR (also called an uber JAR) contains your compiled code, all Maven/Gradle dependencies, AND an embedded Tomcat server — all in a single `.jar` file. That's why you can run it with just `java -jar` and get a fully working web server.

---

#### `EXPOSE 9595`

```dockerfile
EXPOSE 9595
```

- **`EXPOSE`** tells Docker that the container will listen on port `9595` at runtime.
- This is the port Spring Boot is configured to run on (set in `application.properties` via `server.port=9595`).
- **Important:** `EXPOSE` is purely **documentation** — it does NOT actually publish the port or make it accessible from outside the container.
- To actually access it from your browser/host, you still need the `-p` flag when running the container.

> 💡 **`EXPOSE` vs `-p` — they are NOT the same:**
>
> | | `EXPOSE` in Dockerfile | `-p` in `docker run` |
> |---|---|---|
> | What it does | Documents the port the app uses | Actually maps host port → container port |
> | Makes port accessible? | ❌ No | ✅ Yes |
> | Required? | Optional (good practice) | Required to access from outside |
>
> Think of `EXPOSE` as leaving a note saying *"this app uses port 9595"*. The `-p` flag is what actually opens the door.

> ⚠️ **Order matters — `EXPOSE` should come before `CMD`:**  
> While Docker doesn't enforce this, convention is to put `EXPOSE` before `CMD`. The Dockerfile above has them swapped — functionally it works, but best practice is:
> ```dockerfile
> EXPOSE 9595
> CMD ["java", "-jar", "DockerTest-0.0.1-SNAPSHOT.jar"]
> ```

---

### 🌐 Running the Container — `docker run -p 9095:9595 springimage`

```bash
docker run -p 9095:9595 springimage
```

Breaking this down:

```
docker run   -p   9095 : 9595   springimage
                   │       │
              Host Port   Container Port
              (your       (Spring Boot
              machine)     inside Docker)
```

- `9595` → the port Spring Boot listens on **inside** the container (matches `EXPOSE 9595` and `server.port=9595`).
- `9095` → the port you use to access the app **from your browser** on your host machine.
- So hitting `http://localhost:9095/` on your browser → forwards to `9595` inside the container → Spring Boot responds.

> 💡 The host port (left) and container port (right) don't have to match. Here they're intentionally different — `9095` outside, `9595` inside — which is perfectly valid and a good way to avoid port conflicts on your machine.

#### What the API returns at `http://localhost:9095/`:
```json
{
  "message": "Java API is working fine!",
  "languages": ["Java", "Python", "JavaScript"],
  "code": 2345
}
```

This response is built by `TestController.java` — the `getValues()` method returns a `HashMap` which Spring Boot automatically serialises to JSON.

---

### 🧅 What the Final Image Looks Like (Layers)

```
[ CMD — java -jar DockerTest-0.0.1-SNAPSHOT.jar ]  ← starts Spring Boot app
[ EXPOSE — 9595                                 ]  ← documents the port
              │
[ COPY — . → /usr/src/myapp                     ]  ← new layer: entire project copied in
              │                                       (including the pre-built .jar)
[ WORKDIR — /usr/src/myapp                      ]  ← sets working directory
              │
[ LABEL — authors="kshit"                       ]  ← metadata only
              │
[ FROM — eclipse-temurin:21-jdk-jammy           ]  ← base: Ubuntu Jammy + JDK 21
```

---

### ▶️ Full Build and Run Workflow

```bash
# Step 1 — Build the JAR with Maven first
./mvnw clean package -DskipTests
# This produces: target/DockerTest-0.0.1-SNAPSHOT.jar

# Step 2 — Build the Docker image
docker build -t springimage .

# Step 3 — Run the container, mapping host:9095 → container:9595
docker run -p 9095:9595 springimage

# Step 4 — Test it
curl http://localhost:9095/
# or open http://localhost:9095/ in your browser
```

---

### 🏗️ Better Approach — Multi-Stage Build (Advanced)

Right now, the image includes the full JDK (~400MB) even though at runtime you only need the JRE to run the JAR. A **multi-stage build** fixes this — compile with JDK in stage 1, run with JRE in stage 2:

```dockerfile
# Stage 1 — Build: use JDK to build the JAR
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2 — Run: use lightweight JRE to just run the JAR
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/DockerTest-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 9595
CMD ["java", "-jar", "app.jar"]
```

> 💡 This produces a much smaller final image — the JDK, Maven, and source code never make it into the final image. Only the compiled `.jar` is copied over.

---
---

## 🔨 `docker build` — Explained

### The Command

```bash
docker build -t pythonimage .
```

### Breaking it down — every part:

```
docker build  -t  pythonimage  .
    │          │       │        │
    │          │       │        └── Build context — where to find the Dockerfile
    │          │       │             and files to COPY (. = current directory)
    │          │       │
    │          │       └── The name (and optionally tag) to give the image
    │          │            "pythonimage" → name: pythonimage, tag: latest (default)
    │          │            "pythonimage:1.0" → name: pythonimage, tag: 1.0
    │          │
    │          └── -t flag → stands for "tag" — names the resulting image
    │
    └── The docker build command — reads Dockerfile and builds an image
```

---

### What each part means in detail:

#### `docker build`
- The command that **reads a Dockerfile and builds a Docker image** from it.
- It processes each instruction top to bottom, creating a new layer for each `RUN`, `COPY`, and `ADD`.

#### `-t pythonimage`
- `-t` stands for **tag** — it gives your image a human-readable **name** (and optionally a version).
- Without `-t`, Docker still builds the image but assigns it no name — you'd have to reference it by its ugly image ID like `a1b2c3d4e5f6`.
- Format: `-t name:tag`
  ```bash
  docker build -t pythonimage .          # name=pythonimage, tag=latest (default)
  docker build -t pythonimage:1.0 .      # name=pythonimage, tag=1.0
  docker build -t pythonimage:latest .   # same as first line, explicit
  ```

#### `.` (the dot)
- This is the **build context** — the directory Docker sends to the Docker daemon to build the image.
- `.` means the **current directory** — Docker will look for the `Dockerfile` here and make all files in this folder available for `COPY` instructions.
- You can point to a different directory if needed:
  ```bash
  docker build -t pythonimage ./my-app       # Dockerfile is inside ./my-app/
  docker build -t pythonimage -f Dockerfile.prod .   # use a custom Dockerfile name
  ```

> ⚠️ **The dot is easy to forget but mandatory.** Without it, Docker doesn't know where to find your files:
> ```bash
> docker build -t pythonimage     # ❌ Error: "path" argument is required
> docker build -t pythonimage .   # ✅ Correct
> ```

---

### What happens step by step when you run it:

```
docker build -t pythonimage .
        │
        ├── 1. Docker reads the Dockerfile in the current directory
        │
        ├── 2. Sends the build context (all files in .) to the Docker daemon
        │
        ├── 3. Executes each instruction top to bottom:
        │         FROM   → pulls python:latest if not already local
        │         LABEL  → adds metadata
        │         WORKDIR → sets working directory
        │         COPY   → copies your files into the image
        │         CMD    → registers the default startup command
        │
        ├── 4. Each RUN / COPY creates a new cached layer
        │
        └── 5. Tags the final image as "pythonimage:latest"
```

---

### Verifying the build worked:

```bash
# List local images — you should see pythonimage
docker images

# Output:
# REPOSITORY    TAG      IMAGE ID       CREATED         SIZE
# pythonimage   latest   f8d7e1c92b1a   2 seconds ago   1.02GB
```

---

### Common `docker build` variations:

```bash
# Tag with a specific version
docker build -t pythonimage:1.0 .

# Build without cache (forces every step to re-run fresh)
docker build --no-cache -t pythonimage .

# Use a custom Dockerfile name
docker build -f Dockerfile.prod -t pythonimage .

# Pass a build-time variable
docker build --build-arg APP_ENV=production -t pythonimage .
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