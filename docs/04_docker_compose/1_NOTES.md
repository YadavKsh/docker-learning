# 🐙 Docker Compose — Complete Notes

> 📁 Everything you need to know about Docker Compose — what it is, why it exists, how to write a `docker-compose.yml`, and how to use it in real projects.

---

## 📑 Table of Contents

1. [What is Docker Compose?](#1-what-is-docker-compose)
2. [Docker CLI vs Docker Compose](#2-docker-cli-vs-docker-compose)
3. [The docker-compose.yml File](#3-the-docker-composeyml-file)
4. [Core Concepts](#4-core-concepts)
    - [Services](#-services)
    - [Networks](#-networks)
    - [Volumes](#-volumes)
5. [All docker-compose.yml Fields Explained](#5-all-docker-composeyml-fields-explained)
6. [Essential Docker Compose Commands](#6-essential-docker-compose-commands)
7. [Real-World Example — Spring Boot + MySQL](#7-real-world-example--spring-boot--mysql)
8. [Environment Variables in Compose](#8-environment-variables-in-compose)
9. [Build vs Image in Compose](#9-build-vs-image-in-compose)
10. [depends_on — Service Startup Order](#10-depends_on--service-startup-order)
11. [Common Mistakes and Fixes](#11-common-mistakes-and-fixes)
12. [Quick Reference Cheatsheet](#12-quick-reference-cheatsheet)

---
---

## 1. What is Docker Compose?

**Docker Compose** is a tool that lets you define and run **multiple containers as a single application** using one configuration file — `docker-compose.yml`.

Instead of typing long `docker run` commands for each container separately, you describe your entire application setup in one YAML file and bring everything up with a single command.

```
docker-compose.yml
       │
       ▼
  docker compose up
       │
       ├──► Container 1: Spring Boot App  (port 8080)
       ├──► Container 2: MySQL Database   (port 3306)
       └──► Container 3: Redis Cache      (port 6379)
              all on the same network, all started together
```

### 🤔 Why does Docker Compose exist?

Real applications are never just one container. A typical web app has:
- A **backend** (Spring Boot / Node / Python)
- A **database** (MySQL / PostgreSQL / MongoDB)
- A **cache** (Redis)
- Maybe a **reverse proxy** (Nginx)

Without Compose, you'd have to:
1. Create a network manually
2. Create volumes manually
3. Run each container with a long `docker run` command
4. Remember all the env vars, port mappings, and names every single time

With Compose, you define it all **once** in a YAML file and never type those commands again.

---

## 2. Docker CLI vs Docker Compose

Here's the same setup — Spring Boot + MySQL — done both ways:

### ❌ Without Compose (the painful way)

```bash
# Step 1 — create a network
docker network create app-network

# Step 2 — create a volume
docker volume create mysql-data

# Step 3 — run MySQL (long command, easy to mess up)
docker run -d \
  --name my-mysql \
  --network app-network \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=mydb \
  mysql:8.0

# Step 4 — run Spring Boot (another long command)
docker run -d \
  --name my-spring-app \
  --network app-network \
  -p 8080:8080 \
  -e DB_URL=jdbc:mysql://my-mysql:3306/mydb \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  my-spring-app:1.0
```

### ✅ With Compose (the clean way)

```yaml
# docker-compose.yml
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: mydb

  spring-app:
    image: my-spring-app:1.0
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://mysql:3306/mydb
      DB_USERNAME: root
      DB_PASSWORD: secret

volumes:
  mysql-data:
```

```bash
# One command to start everything
docker compose up -d
```

> ✅ Same result. No long commands. No forgetting flags. Just one file and one command.

---

## 3. The `docker-compose.yml` File

The `docker-compose.yml` (or `compose.yml`) is a **YAML file** that describes your entire application stack.

### Basic skeleton:

```yaml
services:          # ← define your containers here
  service-name-1:
    image: ...
    ports: ...
    environment: ...

  service-name-2:
    build: .
    ports: ...
    depends_on:
      - service-name-1

networks:          # ← optional: define custom networks
  my-network:

volumes:           # ← optional: define named volumes
  my-volume:
```

### What is YAML?

YAML (Yet Another Markup Language) is a human-readable format for configuration files. Key rules:
- **Indentation matters** — use 2 spaces (not tabs)
- `key: value` pairs
- Lists use `-` prefix
- Nested items are indented under their parent

```yaml
# Example YAML structure
services:
  my-app:                     # service name
    image: nginx:latest       # key: value
    ports:                    # key with a list value
      - "8080:80"             # list item (note the dash)
      - "443:443"
    environment:              # key with nested key-value pairs
      APP_ENV: production
      LOG_LEVEL: info
```

> 💡 Compose automatically creates a **default network** for all services in the file, so they can reach each other by service name. You don't even need to define one explicitly for basic setups.

---

## 4. Core Concepts

### 🟦 Services

A **service** is one container in your application. Each service in `docker-compose.yml` corresponds to one running container.

```yaml
services:
  web:          # ← service name (becomes the hostname on the network)
    image: nginx:latest

  database:     # ← another service
    image: mysql:8.0
```

- The service name (e.g., `web`, `database`) also acts as the **hostname** — other containers can reach it using this name.
- Each service can have its own image, ports, environment variables, volumes, and network config.

---

### 🟩 Networks

Networks let containers **communicate with each other**. Compose automatically creates a default network for all services in the file.

```yaml
services:
  app:
    image: my-app
    networks:
      - backend-network

  db:
    image: mysql:8.0
    networks:
      - backend-network

networks:
  backend-network:        # define the custom network
    driver: bridge        # bridge is the default and most common
```

- On the same network, services reach each other by their **service name** as the hostname.
- `app` can reach `db` at `db:3306` — no IP addresses needed.
- Services on different networks are isolated from each other.

> 💡 If you don't define any networks, Compose creates one automatically and puts all services on it. For simple projects, you don't need to define networks explicitly.

---

### 🟨 Volumes

Volumes provide **persistent storage** — data that survives container restarts and removals.

```yaml
services:
  db:
    image: mysql:8.0
    volumes:
      - mysql-data:/var/lib/mysql    # named volume
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql  # bind mount

volumes:
  mysql-data:    # declare the named volume here
```

Two types of volume mounts:

| Type | Syntax | What it does |
|---|---|---|
| **Named volume** | `volume-name:/path/in/container` | Docker manages storage; persists across container removal |
| **Bind mount** | `./host/path:/container/path` | Mounts a specific folder from your host into the container |

> 💡 **Named volumes** are for data you want Docker to manage (like database files).  
> **Bind mounts** are for development — e.g., mounting your source code so live changes reflect immediately without rebuilding the image.

---

## 5. All `docker-compose.yml` Fields Explained

Here is a comprehensive reference of the most important fields you'll use:

```yaml
services:
  my-service:

    # ── IMAGE ─────────────────────────────────────────────
    image: mysql:8.0
    # Pull this image from Docker Hub (or local cache).
    # Use either 'image' OR 'build', not both.

    # ── BUILD ─────────────────────────────────────────────
    build: .
    # Build an image from a Dockerfile in the current directory.
    # Use this instead of 'image' when you have your own Dockerfile.
    # Can also be written as:
    # build:
    #   context: ./my-app
    #   dockerfile: Dockerfile.prod

    # ── CONTAINER NAME ────────────────────────────────────
    container_name: my-custom-name
    # Optional. Give the container a specific name.
    # Without this, Compose auto-names it as: projectname-servicename-1

    # ── PORTS ─────────────────────────────────────────────
    ports:
      - "8080:80"       # host:container — same as docker run -p
      - "443:443"
    # Maps host ports to container ports.
    # Quote them to avoid YAML parsing issues with colons.

    # ── ENVIRONMENT VARIABLES ─────────────────────────────
    environment:
      MYSQL_ROOT_PASSWORD: secret
      MYSQL_DATABASE: mydb
    # Set env vars inside the container.
    # Same as docker run -e KEY=VALUE.

    # ── ENV FILE ──────────────────────────────────────────
    env_file:
      - .env
    # Load env vars from a file instead of listing them here.
    # Keeps secrets out of docker-compose.yml.

    # ── VOLUMES ───────────────────────────────────────────
    volumes:
      - mysql-data:/var/lib/mysql     # named volume
      - ./config:/app/config          # bind mount
    # Persist data or mount host files into the container.

    # ── NETWORKS ──────────────────────────────────────────
    networks:
      - app-network
    # Connect this service to one or more networks.

    # ── DEPENDS_ON ────────────────────────────────────────
    depends_on:
      - mysql
    # Start this service only AFTER 'mysql' has started.
    # Note: "started" ≠ "ready" — see Section 10 for details.

    # ── RESTART POLICY ────────────────────────────────────
    restart: always
    # Options: no | always | on-failure | unless-stopped
    # 'always'         → restart on crash AND on Docker daemon restart
    # 'on-failure'     → restart only if exit code is non-zero
    # 'unless-stopped' → restart always except when manually stopped

    # ── COMMAND ───────────────────────────────────────────
    command: python3 main.py
    # Override the default CMD from the Dockerfile.
    # Same as passing a command at the end of docker run.

    # ── ENTRYPOINT ────────────────────────────────────────
    entrypoint: /app/start.sh
    # Override the ENTRYPOINT from the Dockerfile.

    # ── WORKING DIRECTORY ─────────────────────────────────
    working_dir: /usr/src/myapp
    # Override WORKDIR from the Dockerfile.

    # ── EXPOSE (informational only) ───────────────────────
    expose:
      - "9595"
    # Documents which port the container uses internally.
    # Does NOT publish it to the host — use 'ports' for that.

    # ── STDIN / TTY ───────────────────────────────────────
    stdin_open: true    # same as docker run -i
    tty: true           # same as docker run -t
    # Use together for an interactive container (like -it).

    # ── HEALTHCHECK ───────────────────────────────────────
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    # Periodically test if the service is healthy.
    # Useful with depends_on condition: service_healthy.

    # ── LABELS ────────────────────────────────────────────
    labels:
      app.version: "1.0"
      maintainer: "kshit"
    # Add metadata to the container (same as LABEL in Dockerfile).

volumes:
  mysql-data:     # declare named volumes used by services here

networks:
  app-network:    # declare custom networks used by services here
    driver: bridge
```

---

## 6. Essential Docker Compose Commands

### Starting and Stopping

```bash
# Start all services (foreground — logs stream to terminal)
docker compose up

# Start all services in background (detached)
docker compose up -d

# Stop and remove all containers, networks (volumes kept)
docker compose down

# Stop and remove containers + volumes (⚠️ deletes data!)
docker compose down -v

# Stop all services without removing them
docker compose stop

# Start already-created (stopped) services
docker compose start
```

### Building

```bash
# Build images defined with 'build:' in compose file
docker compose build

# Build a specific service only
docker compose build spring-app

# Force rebuild without cache
docker compose build --no-cache
```

### Monitoring

```bash
# View logs for all services
docker compose logs

# Follow live logs (like tail -f)
docker compose logs -f

# Follow logs for a specific service
docker compose logs -f spring-app

# Show last 50 lines
docker compose logs --tail 50

# List running containers managed by this compose file
docker compose ps
```

### Interacting with Services

```bash
# Open a shell inside a running service
docker compose exec spring-app /bin/bash
docker compose exec mysql /bin/bash

# Run a one-off command in a new container (doesn't need the service running)
docker compose run spring-app java --version

# Restart a specific service
docker compose restart spring-app

# Scale a service to multiple instances
docker compose up -d --scale spring-app=3
```

### Teardown

```bash
# Remove stopped containers
docker compose rm

# Stop everything and remove containers + networks + images
docker compose down --rmi all

# Stop everything and remove containers + networks + images + volumes
docker compose down --rmi all -v
```

---

## 7. Real-World Example — Spring Boot + MySQL

This is a complete, production-style `docker-compose.yml` for a Spring Boot app backed by MySQL.

### Project structure:

```
my-project/
├── docker-compose.yml
├── .env
├── Dockerfile
└── target/
    └── myapp-0.0.1-SNAPSHOT.jar
```

### `.env` file (keep this out of git!):

```env
MYSQL_ROOT_PASSWORD=supersecret
MYSQL_DATABASE=appdb
DB_USERNAME=root
DB_PASSWORD=supersecret
```

### `docker-compose.yml`:

```yaml
services:

  mysql:
    image: mysql:8.0
    container_name: app-mysql
    restart: unless-stopped
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
    networks:
      - app-network
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  spring-app:
    build: .
    container_name: app-spring
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      DB_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
    depends_on:
      mysql:
        condition: service_healthy
    networks:
      - app-network

volumes:
  mysql-data:

networks:
  app-network:
    driver: bridge
```

### Run it:

```bash
# Start everything
docker compose up -d

# Check status
docker compose ps

# Watch Spring Boot logs
docker compose logs -f spring-app

# Shut everything down (keep data)
docker compose down

# Shut everything down AND delete database data
docker compose down -v
```

### What Compose does automatically here:
- Creates the `app-network` network
- Creates the `mysql-data` volume
- Starts `mysql` first (because of `depends_on`)
- Waits until MySQL passes its healthcheck before starting `spring-app`
- Both containers can reach each other using service names as hostnames

---

## 8. Environment Variables in Compose

There are three ways to pass environment variables to services in Compose:

### Method 1 — Inline in `docker-compose.yml`

```yaml
environment:
  MYSQL_ROOT_PASSWORD: secret
  APP_ENV: production
```

> ⚠️ Don't put real secrets here — this file often goes into version control.

---

### Method 2 — Reference a `.env` file

```yaml
env_file:
  - .env
```

```env
# .env
MYSQL_ROOT_PASSWORD=secret
APP_ENV=production
```

> ✅ Safer — add `.env` to `.gitignore` so secrets never get pushed to GitHub.

---

### Method 3 — Variable substitution from `.env`

Compose automatically reads a `.env` file in the same directory and lets you reference variables with `${}`:

```env
# .env
DB_PASS=secret123
```

```yaml
# docker-compose.yml
environment:
  DB_PASSWORD: ${DB_PASS}
```

> 💡 This is the cleanest approach — your `docker-compose.yml` contains no actual secrets, just variable references. Secrets live in `.env` which is gitignored.

### Priority order (highest to lowest):

```
1. Shell environment variables (export KEY=val before docker compose up)
2. Variables in .env file
3. Default values defined in docker-compose.yml
```

---

## 9. `build` vs `image` in Compose

When defining a service, you choose between pulling a pre-built image or building one from a Dockerfile:

### Using `image` — pull from Docker Hub or local cache

```yaml
services:
  mysql:
    image: mysql:8.0       # use a pre-built image
```

Use this when:
- You're using a third-party image (MySQL, Redis, Nginx, etc.)
- Your image is already built and pushed to a registry

---

### Using `build` — build from a Dockerfile

```yaml
services:
  spring-app:
    build: .               # Dockerfile is in current directory
```

Or with more options:

```yaml
services:
  spring-app:
    build:
      context: ./my-app          # directory containing Dockerfile
      dockerfile: Dockerfile.prod  # custom Dockerfile name
      args:
        APP_ENV: production       # build arguments passed to Dockerfile
```

Use this when:
- You have your own application with a Dockerfile
- You want Compose to build the image for you automatically

---

### Using both — build and tag the image

```yaml
services:
  spring-app:
    build: .
    image: kshit/spring-app:1.0    # also tag it with this name
```

This builds from the Dockerfile AND tags the resulting image as `kshit/spring-app:1.0` — useful when you want to also push it to Docker Hub later.

---

## 10. `depends_on` — Service Startup Order

`depends_on` controls **which services start before others**. But there's an important distinction:

### Basic `depends_on` — waits for container to START (not be ready)

```yaml
services:
  spring-app:
    depends_on:
      - mysql    # waits for mysql container to START, not to be ready
```

> ⚠️ **The problem:** MySQL takes several seconds to initialise after the container starts. With basic `depends_on`, Spring Boot might start connecting to MySQL before MySQL is actually ready to accept connections — causing connection errors.

---

### `depends_on` with `condition: service_healthy` — waits until READY ✅

```yaml
services:
  mysql:
    image: mysql:8.0
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  spring-app:
    depends_on:
      mysql:
        condition: service_healthy    # waits until MySQL passes healthcheck
```

This is the correct way — Spring Boot only starts once MySQL is **actually ready to accept connections**.

### Condition options:

| Condition | Meaning |
|---|---|
| `service_started` | Default — wait for container to start only |
| `service_healthy` | Wait until healthcheck passes ✅ recommended |
| `service_completed_successfully` | Wait until service exits with code 0 (for init jobs) |

---

## 11. Common Mistakes and Fixes

| Mistake | Error / Symptom | Fix |
|---|---|---|
| Using `localhost` for DB_URL | `Connection refused` | Use the **service name** as hostname: `jdbc:mysql://mysql:3306/db` |
| Forgetting to declare volumes | Data lost on `docker compose down` | Add named volume in `volumes:` section and mount it |
| Using `depends_on` without healthcheck | Spring Boot crashes on startup (DB not ready) | Add `healthcheck` to DB service and use `condition: service_healthy` |
| Secrets in `docker-compose.yml` | Credentials pushed to GitHub | Move secrets to `.env` file, add `.env` to `.gitignore` |
| Indentation errors in YAML | `yaml.scanner.ScannerError` | Use 2 spaces, never tabs |
| Forgetting to rebuild after code change | Running old code | Run `docker compose up -d --build` to force rebuild |
| Port already in use | `bind: address already in use` | Change the host port (left side of `host:container`) |
| `docker compose` vs `docker-compose` | `command not found` | Modern Docker uses `docker compose` (no hyphen). `docker-compose` is the older standalone tool |

---

## 12. Quick Reference Cheatsheet

### Commands

| Command | What it does |
|---|---|
| `docker compose up` | Start all services (foreground) |
| `docker compose up -d` | Start all services (background) |
| `docker compose up -d --build` | Rebuild images and start |
| `docker compose down` | Stop and remove containers + networks |
| `docker compose down -v` | Same + delete volumes (data!) |
| `docker compose stop` | Stop containers without removing |
| `docker compose start` | Start stopped containers |
| `docker compose restart` | Restart all services |
| `docker compose ps` | List service containers |
| `docker compose logs -f` | Follow all logs |
| `docker compose logs -f svc` | Follow logs for one service |
| `docker compose exec svc bash` | Shell into a running service |
| `docker compose build` | Build all services with `build:` |
| `docker compose build --no-cache` | Force fresh build |
| `docker compose pull` | Pull latest images |
| `docker compose rm` | Remove stopped containers |

### `docker-compose.yml` fields

| Field | What it does |
|---|---|
| `image` | Use a pre-built image |
| `build` | Build from Dockerfile |
| `container_name` | Custom container name |
| `ports` | Map `host:container` ports |
| `environment` | Set env vars |
| `env_file` | Load env vars from a file |
| `volumes` | Mount named volumes or bind mounts |
| `networks` | Connect to networks |
| `depends_on` | Set startup order |
| `restart` | Restart policy |
| `command` | Override CMD |
| `healthcheck` | Health test for the service |

### Restart policies

| Value | Behaviour |
|---|---|
| `no` | Never restart (default) |
| `always` | Always restart, even on Docker daemon restart |
| `on-failure` | Restart only on non-zero exit |
| `unless-stopped` | Restart always unless manually stopped |

---

> 💡 **Golden Rule of Docker Compose:**  
> One `docker-compose.yml` = your entire application stack.  
> One `docker compose up -d` = everything running.  
> One `docker compose down` = everything stopped and cleaned up.