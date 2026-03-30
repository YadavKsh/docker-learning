# 🐳 Docker Commands — Complete Reference

A complete breakdown of all essential Docker CLI commands — from pulling images to deploying containers in production.

---

> 💡 **How to read this file:**
> - `<Image_Name>` → replace with actual image name e.g. `mysql`, `nginx`, `openjdk`
> - `<Version / Tag>` → replace with actual version e.g. `8.0`, `latest`, `17-jdk-slim`
> - `<Container_ID>` → replace with actual container ID e.g. `a1b2c3d4e5f6`
> - `<Container_Name>` → replace with the name assigned via `--name` e.g. `my-mysql`
> - `//` → these are just comments, not part of the actual command

---

## 📑 Table of Contents

### 🖼️ Image Commands
1. [Check Docker Version](#1--check-docker-version)
2. [Pull an Image](#2--pull-an-image)
3. [Pull an Image with a Specific Version / Tag](#3--pull-an-image-with-a-specific-version--tag)
4. [See All Local Images](#4--see-all-local-images)
5. [Search for an Image on Docker Hub](#5--search-for-an-image-on-docker-hub)
6. [Build an Image from a Dockerfile](#6-️-docker-build--build-an-image-from-a-dockerfile)
7. [Remove Images](#7-️-docker-rmi--remove-images)
8. [Tag / Rename an Image](#8-️-docker-tag--rename--retag-an-image)

### 🚀 Container Commands
9. [docker run](#9--the-docker-run-command--master-reference)
10. [Port Mapping — `-p`](#10--docker-run--p----port-mapping)
11. [Run in Background — `-d`](#11--docker-run--d----detach)
12. [Name a Container — `--name`](#12-️-docker-run---name)
13. [Set Environment Variables — `-e`](#13--docker-run--e----env)
14. [Interactive Shell — `-it`](#14-💻-docker-run--it)
15. [List Running Containers — `docker ps`](#15--docker-ps)
16. [List All Containers — `docker ps -a`](#16--docker-ps--a)
17. [Start a Stopped Container](#17-️-docker-start--restart-a-stopped-container)
18. [Stop a Container](#18-️-docker-stop)
19. [Restart a Container](#19--docker-restart)
20. [Remove Containers](#20-️-docker-rm--remove-containers)
21. [Execute Commands in a Running Container](#21--docker-exec)
22. [Inspect a Container or Image](#22--docker-inspect)
23. [View Container Logs](#23--docker-logs)
24. [Live Resource Usage](#24--docker-stats--live-resource-usage)
25. [Copy Files To / From a Container](#25--docker-cp--copy-files)

### 🗄️ Registry Commands
26. [Login](#26--docker-login)
27. [Commit a Container to an Image](#27--docker-commit)
28. [Push an Image](#28--docker-push)
29. [Logout](#29--docker-logout)

### 🌐 Networking & Storage
30. [Container Networking](#30--docker-network--container-networking)
31. [Persistent Storage — Volumes](#31-️-docker-volume--persistent-storage)

### 🧹 Cleanup
32. [System Prune — Clean Up Everything](#32--docker-system-prune--clean-up-everything)

### 📚 Reference
- [Combining Flags — All Permutations](#-combining-flags--all-permutations)
- [Full Quick Reference Table](#-full-quick-reference-table)
- [Complete Real-World Workflow](#-complete-real-world-workflow)
- [Common Mistakes and Fixes](#️-common-mistakes-and-fixes)
- [Concept Clarity: Base OS vs Host OS vs Runtime](#-concept-clarity-what-is-base-os)

---
---

# 🖼️ Image Commands

---

## 1. 🔍 Check Docker Version

```bash
docker -v
# or
docker --version
```

### What it does:
- Confirms that Docker is **installed** on your machine.
- Shows the **version number** of Docker currently installed.

### Example Output:
```
Docker version 24.0.5, build ced0996
```

### When to use it:
- First thing to run after installing Docker — to verify installation was successful.
- When debugging issues — version mismatches can cause unexpected behaviour.
- When following tutorials — some features require specific Docker versions.

> 💡 `-v` is the short form, `--version` is the long form. Both do exactly the same thing. This is a common CLI convention — most tools support both.

---

## 2. 📥 Pull an Image

```bash
docker pull <Image_Name>
```

### What it does:
- **Downloads** a Docker image from **Docker Hub** (the default public registry) to your local machine.
- If no version/tag is specified, Docker automatically pulls the **`latest`** tag.
- The image is stored in your local image cache so you can create containers from it.

### Example:
```bash
docker pull mysql
# pulls mysql:latest (most recent version)

docker pull nginx
# pulls nginx:latest

docker pull openjdk
# pulls openjdk:latest
```

### Example Output:
```
Using default tag: latest
latest: Pulling from library/mysql
e1caac4eb9d2: Pull complete
...
Status: Downloaded newer image for mysql:latest
docker.io/library/mysql:latest
```

### What each line means:
| Output | Meaning |
|--------|---------|
| `Using default tag: latest` | No tag was specified, so Docker defaults to `latest` |
| `Pulling from library/mysql` | Downloading from the official MySQL repo on Docker Hub |
| `e1caac4eb9d2: Pull complete` | One image layer downloaded successfully |
| `Status: Downloaded newer image` | Image pulled fresh for the first time |
| `Status: Image is up to date` | You already had the latest version, nothing downloaded |

### How layers work during pull:
```
📦 Docker Hub (mysql:latest)
         │
         ▼
  [ Layer 1: Base OS        ]
         │
         ▼
  [ Layer 2: MySQL binaries ]
         │
         ▼
  [ Layer 3: Config files   ]
         │
         ▼
  [ Layer 4: Startup scripts]
         │
         ▼
  💻 Your Local Machine (image cache)
```

> ⚡ **Layers are cached!** If you pull another image that shares a layer with one you already have (e.g., both use the same base OS), Docker skips downloading that layer — saving time and disk space.

---

## 3. 📥 Pull an Image with a Specific Version / Tag

```bash
docker pull <Image_Name>:<Version / Tag>
```

### What it does:
- Same as `docker pull` but downloads a **specific version** of the image instead of `latest`.
- The version/tag comes after the `:` (colon).
- Tags are defined by the image publisher on Docker Hub.

### Example:
```bash
docker pull mysql:8.0
# pulls MySQL version 8.0 specifically

docker pull openjdk:17-jdk-slim
# pulls OpenJDK 17, slim variant (smaller size)

docker pull openjdk:11
# pulls OpenJDK 11

docker pull nginx:1.25-alpine
# pulls Nginx 1.25, Alpine Linux variant (very small)
```

### Why use a specific version instead of `latest`?

| Using `latest` | Using specific version |
|---------------|----------------------|
| Always gets newest version | Gets exactly what you specify |
| Can break your app if new version has changes | Predictable, stable |
| Good for quick experiments | ✅ Recommended for real projects |
| `latest` today ≠ `latest` tomorrow | Same image every time |

> ⚠️ **Best Practice:** Always pin to a specific version in production. If you use `mysql:latest` today and someone rebuilds the image tomorrow after MySQL releases a new version, your app might break. Use `mysql:8.0` to be safe.

### Common Tags You'll See:
| Tag | Meaning |
|-----|---------|
| `latest` | Most recent stable release (default) |
| `8.0`, `17`, `3.10` | Specific version number |
| `slim` | Smaller image, fewer tools pre-installed |
| `alpine` | Based on Alpine Linux — extremely small (5MB base) |
| `lts` | Long Term Support version |
| `jdk` vs `jre` | Full Java Dev Kit vs just the Runtime Environment |

---

## 4. 🗂️ See All Local Images

```bash
docker images
```

### What it does:
- Lists **all Docker images** currently stored on your local machine.
- Shows useful metadata about each image.

### Example Output:
```
REPOSITORY    TAG          IMAGE ID       CREATED        SIZE
mysql         8.0          3218b38490ce   2 weeks ago    516MB
openjdk       17-jdk-slim  1b6a3e3b24e7   3 weeks ago    409MB
nginx         latest       a8758716bb6a   4 weeks ago    187MB
my-spring-app 1.0          f8d7e1c92b1a   2 days ago     298MB
```

### What each column means:
| Column | Meaning |
|--------|---------|
| `REPOSITORY` | The image name (e.g., `mysql`, `nginx`, your custom image) |
| `TAG` | The version/tag of the image |
| `IMAGE ID` | Unique identifier for the image (shortened hash) |
| `CREATED` | When the image was built/pulled |
| `SIZE` | Disk space the image takes up |

### Useful variations:
```bash
# Show only image IDs (useful for scripting)
docker images -q

# Show all images including intermediate layers
docker images -a

# Filter images by name
docker images mysql
```

---

## 5. 🔎 Search for an Image on Docker Hub

```bash
docker search <Image_Name>
```

### What it does:
- **Searches Docker Hub** for images matching the given name — directly from your terminal.
- Saves you from opening a browser to visit hub.docker.com.
- Shows official images, community images, ratings, and descriptions.

### Example:
```bash
docker search mysql
docker search java
docker search springboot
```

### Example Output:
```
NAME                            DESCRIPTION                                     STARS     OFFICIAL
mysql                           MySQL is a widely used, open-source relation…   14893     [OK]
mariadb                         MariaDB Server is a high performing open sou…   5673      [OK]
bitnami/mysql                   Bitnami MySQL Docker Image                      105
mysql/mysql-server              Optimized MySQL Server Docker images.           971
```

### What each column means:
| Column | Meaning |
|--------|---------|
| `NAME` | Image name (use this in `docker pull`) |
| `DESCRIPTION` | Brief description of the image |
| `STARS` | Community rating — higher = more trusted/popular |
| `OFFICIAL` | `[OK]` means it's an **official image** maintained by the software vendor |

### Official vs Community Images:
| | Official Images | Community Images |
|-|----------------|-----------------|
| **Maintained by** | The software vendor (e.g., MySQL team) | Individual developers or organizations |
| **Security** | Regularly updated, security-patched | Varies |
| **Trust** | ✅ Highly trusted | Use with caution |
| **Example** | `mysql`, `nginx`, `openjdk` | `bitnami/mysql`, `linuxserver/mysql` |

> ✅ **Always prefer Official images** (marked `[OK]`) when available.

### Filter search results:
```bash
# Show only official images
docker search --filter "is-official=true" mysql

# Show images with at least 100 stars
docker search --filter "stars=100" mysql

# Limit results to top 5
docker search --limit 5 mysql
```

---

## 6. 🏗️ `docker build` — Build an Image from a Dockerfile

```bash
docker build -t <image_name>:<tag> <path_to_dockerfile_directory>
docker build -t my-spring-app:1.0 .     # . means current directory
```

### What it does:
- **Builds a Docker image** from a `Dockerfile` in the specified directory.
- `-t` tags the resulting image with a name and version.
- The `.` at the end tells Docker where to find the `Dockerfile` (current directory).
- Each instruction in the `Dockerfile` becomes a new layer in the image.

### Example:
```bash
docker build -t my-spring-app:1.0 .
docker build -t my-nginx:prod ./docker/nginx
```

### Useful build flags:
```bash
# Build without using cache (fresh build every layer)
docker build --no-cache -t my-app:1.0 .

# Use a custom Dockerfile name/location
docker build -f Dockerfile.prod -t my-app:prod .

# Pass build-time arguments
docker build --build-arg JAR_FILE=target/app.jar -t my-app:1.0 .
```

### `docker build` vs `docker commit`:
| | `docker build` | `docker commit` |
|-|---------------|----------------|
| How | Reads `Dockerfile` instructions | Snapshots a running container |
| Reproducible? | ✅ Yes — same image every time | ❌ No — hard to recreate |
| Good for | ✅ All real projects | Quick experiments only |
| Versioning | Every step documented in Dockerfile | Hard to track changes |

---

## 7. 🖼️ `docker rmi` — Remove Images

```bash
docker rmi <Image_Name>
docker rmi <Image_Name>:<Tag>
docker rmi <Image_ID>

# Remove multiple images
docker rmi mysql:8.0 nginx:latest openjdk:17
```

### What it does:
- **Deletes a Docker image** from your local machine.
- Frees up disk space (images can be hundreds of MBs each).
- `rmi` = "remove image" (different from `rm` which removes containers).
- Will **refuse to delete** an image if a container (even a stopped one) is still using it.

### Example:
```bash
docker rmi mysql:8.0
docker rmi nginx
docker rmi 3218b38490ce    # using the image ID
```

### Error: image is in use
```bash
docker rmi mysql:8.0
# Error: conflict: unable to delete — image is being used by stopped container a1b2c3

# Fix — remove the container first, then the image
docker rm a1b2c3d4e5f6
docker rmi mysql:8.0

# Or force it (not recommended — can leave dangling references)
docker rmi -f mysql:8.0
```

### `docker rm` vs `docker rmi`:
| | `docker rm` | `docker rmi` |
|-|------------|-------------|
| Removes | Container | Image |
| Frees what | Container's writable layer | The full image layers |
| Prereq | Container must be stopped | No containers using it |

### Clean up unused images:
```bash
# Remove all dangling images (untagged, unreferenced layers)
docker image prune

# Remove ALL unused images (not just dangling)
docker image prune -a
```

---

## 8. 🏷️ `docker tag` — Rename / Retag an Image

```bash
docker tag <source_image>:<tag> <new_name>:<new_tag>
```

### What it does:
- Creates a new alias (tag) for an existing image — doesn't copy or duplicate it.
- Required before pushing to Docker Hub (image name must start with your Docker Hub username).

### Example:
```bash
docker tag my-spring-app:1.0 kshitij123/my-spring-app:1.0
docker tag my-spring-app:1.0 kshitij123/my-spring-app:latest
```

---
---

# 🚀 Container Commands

---

## 9. 📌 The `docker run` Command — Master Reference

Before explaining each flag individually, understand the **full anatomy** of a `docker run` command:

```bash
docker run [OPTIONS] IMAGE_NAME [COMMAND] [ARG...]
#           ↑                    ↑
#     flags go here         image name always
#     (in any order)        comes AFTER flags
```

### 📌 Golden Rules of Argument Order:
1. `docker run` always comes first
2. All **flags/options** (like `-d`, `--name`, `-e`, `-p`) come **before** the image name
3. The **image name** always comes **after** all flags
4. Any optional **command to override** inside the container comes **after** the image name

```bash
# ✅ Correct order
docker run -d -p 8081:8081 --name my-app -e DB_URL=localhost mysql:8.0

# ❌ Wrong — image name in the middle
docker run -d mysql:8.0 --name my-app

# ❌ Wrong — flags after image name
docker run mysql:8.0 -d --name my-app
```

### What happens step by step:
```
docker run mysql:8.0
      │
      ├── 1. Check if mysql:8.0 image exists locally
      │         ├── YES → use it
      │         └── NO  → automatically pull from Docker Hub
      │
      ├── 2. Create a new container from the image
      ├── 3. Allocate a writable layer on top of the image
      ├── 4. Set up container networking
      └── 5. Start the container and run ENTRYPOINT command
```

> ⚠️ **`docker run` vs `docker start`:**
> - `docker run` = **creates a brand new container** from an image and starts it
> - `docker start` = **restarts an existing stopped container** (doesn't create a new one)
    > Every time you `docker run`, a new container is born. If you run it 5 times, you get 5 separate containers.

---

## 10. 🔌 `docker run -p` / `--publish` — Port Mapping

```bash
docker run -p <host_port>:<container_port> <image_name>
docker run --publish <host_port>:<container_port> <image_name>
```

### What it does:
- **Binds a port on your host machine** to a port inside the container.
- Without `-p`, the container's ports are **completely isolated** — nothing outside can reach them.
- Format is always `host_port:container_port` — think "outside:inside".

### Example:
```bash
docker run -p 8080:80 nginx
# Your browser accesses localhost:8080 → forwards to container's port 80

docker run -p 3306:3306 mysql:8.0
# Connects to MySQL on localhost:3306 → container's MySQL port 3306

docker run -p 8081:8081 my-spring-app:1.0
# Spring Boot on localhost:8081 → container's 8081
```

### What each part means:
```
-p  8080 : 80
    ↑       ↑
 Host     Container
 Port     Port
(your     (inside
machine)  Docker)
```

### Mapping multiple ports:
```bash
# Repeat -p for each port you want to expose
docker run -p 8080:80 -p 443:443 nginx

# Map to a different host port (if 3306 is already in use on your machine)
docker run -p 3307:3306 mysql:8.0
# Access via localhost:3307, but MySQL inside still listens on 3306
```

### Port mapping explained visually:
```
Your Machine (Host)            Docker Container
─────────────────              ─────────────────
localhost:8080    ──────────►  :80  (nginx)
localhost:3306    ──────────►  :3306 (mysql)
localhost:8081    ──────────►  :8081 (spring-boot)
```

> 💡 **Rule of thumb:** The host port (left side) is what YOU type in the browser. The container port (right side) is what the app inside listens on. You can change the left side freely — the right side must match the app's config.

---

## 11. 🔇 `docker run -d` / `--detach`

```bash
docker run -d <image_name>
docker run --detach <image_name>
```

### What it does:
- Runs the container in **background (detached) mode** — your terminal is freed immediately.
- Without `-d`, the container runs in the **foreground** — your terminal is locked, `Ctrl+C` stops the container.
- With `-d`, Docker prints the **container ID** and returns control to your terminal.

### Foreground vs Detached:
```bash
# Foreground — terminal locked, Ctrl+C stops container
docker run mysql:8.0

# Detached — terminal free, container runs in background
docker run -d mysql:8.0
# prints: a1b2c3d4e5f6e7f8a9b0c1d2e3f4a5b6...
```

> 💡 **Rule of thumb:** Almost always use `-d` in real usage. Skip it only when you want to see live output directly in your terminal for quick debugging.

### See logs of a detached container:
```bash
docker logs my-app           # print all logs
docker logs -f my-app        # -f = follow (live streaming)
```

---

## 12. 🏷️ `docker run --name`

```bash
docker run --name <your_custom_name> <image_name>
```

### What it does:
- Assigns a **custom name** to the container instead of a random auto-generated one like `quirky_darwin`.
- Makes referencing the container in other commands much easier.

### Example:
```bash
# Without --name → Docker assigns a random, useless name
docker run -d mysql:8.0
# Container name: relaxed_hopper

# With --name → clear and meaningful
docker run -d --name my-mysql mysql:8.0
# Container name: my-mysql
```

### Why naming matters:
```bash
# Without a name — need to use the container ID everywhere
docker stop a1b2c3d4e5f6
docker logs a1b2c3d4e5f6
docker exec -it a1b2c3d4e5f6 /bin/bash

# With --name — human-readable
docker stop my-mysql
docker logs my-mysql
docker exec -it my-mysql /bin/bash
```

> ⚠️ Names must be **unique** across all containers. Remove the old container with `docker rm` before reusing a name.

---

## 13. 🔧 `docker run -e` / `--env`

```bash
docker run -e KEY=VALUE <image_name>
docker run --env KEY=VALUE <image_name>

# Multiple env vars — repeat the flag
docker run -e KEY1=VALUE1 -e KEY2=VALUE2 <image_name>

# Load all env vars from a file
docker run --env-file .env <image_name>
```

### What it does:
- Sets **environment variables** inside the container at runtime.
- `-e` and `--env` are identical — short and long form.
- These are accessible to the application running inside the container.
- Overrides any `ENV` instruction set in the Dockerfile.

### Real Example — Running MySQL with required env vars:
```bash
docker run -e MYSQL_ROOT_PASSWORD=secret \
           -e MYSQL_DATABASE=mydb \
           -e MYSQL_USER=kshitij \
           -e MYSQL_PASSWORD=pass123 \
           mysql:8.0
```

> MySQL's official image **requires** `MYSQL_ROOT_PASSWORD` to be passed — it won't start without it.

### Real Example — Running your Spring Boot app:
```bash
docker run -e DB_URL=jdbc:mysql://localhost:3306/myhiber \
           -e DB_USERNAME=root \
           -e DB_PASSWORD=secret \
           my-spring-app:1.0
```

### Using `--env-file` instead of repeating `-e`:
```env
# .env file
DB_URL=jdbc:mysql://localhost:3306/myhiber
DB_USERNAME=root
DB_PASSWORD=secret
```
```bash
docker run --env-file .env my-spring-app:1.0
```

> 🔒 Add `.env` to `.gitignore` — never push it to GitHub!

---

## 14. 💻 `docker run -it`

```bash
docker run -it <image_name>
docker run -it <image_name> /bin/bash    # most Linux images
docker run -it <image_name> /bin/sh      # Alpine-based images (no bash)
```

### What it does:
- `-i` = `--interactive` → keeps **STDIN open** so you can type into the container
- `-t` = `--tty` → allocates a **pseudo-terminal**, giving you a shell prompt
- Combined as `-it` → gives you a **fully interactive shell** inside the container

### `-i` vs `-t` vs `-it`:
| Flag | Result |
|------|--------|
| Neither | Container runs silently, no interaction |
| `-i` only | Can pipe input but no shell prompt |
| `-t` only | Has prompt but can't type |
| `-it` | ✅ Full interactive shell — use this |

### Example — explore inside a container:
```bash
docker run -it ubuntu:22.04 /bin/bash

# Now you're INSIDE the container:
root@a1b2c3d4:/# ls
root@a1b2c3d4:/# java --version
root@a1b2c3d4:/# exit    # exits shell AND stops the container
```

> 💡 Use `-it` for **exploration and debugging**. Use `-d` for running apps in production.

---

## 15. 📋 `docker ps`

```bash
docker ps
```

### What it does:
- Lists all **currently running** containers only.
- Shows key metadata about each running container.

### Example Output:
```
CONTAINER ID   IMAGE         COMMAND                  CREATED        STATUS        PORTS                    NAMES
a1b2c3d4e5f6   mysql:8.0     "docker-entrypoint.s…"   2 hours ago    Up 2 hours    0.0.0.0:3306->3306/tcp   my-mysql
f6e5d4c3b2a1   nginx:latest  "/docker-entrypoint.…"   5 minutes ago  Up 5 minutes  0.0.0.0:80->80/tcp       my-nginx
```

### What each column means:
| Column | Meaning |
|--------|---------|
| `CONTAINER ID` | Unique short ID of the container (first 12 chars of full ID) |
| `IMAGE` | Which image the container was created from |
| `COMMAND` | The command running inside the container (from `ENTRYPOINT`) |
| `CREATED` | How long ago the container was created |
| `STATUS` | Current state — `Up X hours`, `Exited (0)`, `Paused`, etc. |
| `PORTS` | Port mappings — `0.0.0.0:8081->8081/tcp` means host:8081 → container:8081 |
| `NAMES` | Container name — auto-generated or set with `--name` |

---

## 16. 📋 `docker ps -a`

```bash
docker ps -a
# or
docker ps --all
```

### What it does:
- Lists **ALL containers** — running, stopped, exited, and created.
- `-a` = `--all` (short and long form, both work the same).
- Without `-a`, you only see running containers.

### Example Output:
```
CONTAINER ID   IMAGE         STATUS                     NAMES
a1b2c3d4e5f6   mysql:8.0     Up 2 hours                 my-mysql
f6e5d4c3b2a1   nginx         Exited (0) 23 hours ago    old-nginx
9z8y7x6w5v4u   openjdk:17    Exited (1) 2 days ago      crashed-app
```

### Status values explained:
| Status | Meaning |
|--------|---------|
| `Up X hours` | Container is currently running |
| `Exited (0)` | Stopped normally — exit code 0 = success |
| `Exited (1)` | Stopped with an error — non-zero = failure |
| `Created` | Container created but never started |
| `Paused` | Container is paused |

> 💡 Use `docker ps -a` to find stopped containers you want to restart (`docker start`) or clean up (`docker rm`).

---

## 17. ▶️ `docker start` — Restart a Stopped Container

```bash
docker start <Container_Name>
docker start <Container_ID>
```

### What it does:
- **Restarts an existing stopped container** — does NOT create a new one.
- The container resumes with its original configuration (ports, env vars, name).
- Use `docker ps -a` to find stopped containers first.

### Example:
```bash
docker start my-mysql
docker start -a my-mysql     # -a = attach (see output in terminal)
```

### `docker start` vs `docker run`:
| | `docker start` | `docker run` |
|-|---------------|-------------|
| Creates new container? | ❌ No | ✅ Yes |
| Uses existing config? | ✅ Yes — same ports, envs, name | Needs all flags again |
| Use case | Resume a stopped container | Launch a fresh container |

---

## 18. ⏹️ `docker stop`

```bash
docker stop <Container_Name>
docker stop <Container_ID>

# Stop multiple containers at once
docker stop container1 container2 container3
```

### What it does:
- **Gracefully stops** a running container.
- Sends a `SIGTERM` signal to the main process inside — gives it time to clean up.
- If the container doesn't stop within 10 seconds, Docker forcefully kills it with `SIGKILL`.
- The container **still exists** after stopping — it just isn't running. Use `docker ps -a` to see it.

### Example:
```bash
docker stop my-mysql
docker stop a1b2c3d4e5f6
```

### `docker stop` vs `docker kill`:
| | `docker stop` | `docker kill` |
|-|--------------|--------------|
| Signal sent | `SIGTERM` (graceful) | `SIGKILL` (force) |
| Cleanup time | Up to 10 seconds | Immediate |
| Data safety | ✅ Safer — app can flush writes | ⚠️ Risk of data loss |
| When to use | Normal shutdown | Container is completely frozen |

### Change the timeout:
```bash
# Give the container 30 seconds to shut down gracefully before force-killing
docker stop --time 30 my-mysql
docker stop -t 30 my-mysql
```

> ⚠️ **Remember:** `docker stop` does NOT delete the container. It only pauses it. The container and all its data still exist — use `docker start` to bring it back, or `docker rm` to delete it.

---

## 19. 🔄 `docker restart`

```bash
docker restart <Container_Name>
docker restart <Container_ID>

# Restart multiple containers
docker restart container1 container2
```

### What it does:
- **Stops and then immediately starts** a container in one command.
- Equivalent to running `docker stop` followed by `docker start`.
- The container **keeps the same ID, name, and configuration** — it's the same container, just restarted.
- Useful when an app has crashed, needs a config refresh, or is consuming too much memory.

### Example:
```bash
docker restart my-spring-app
docker restart my-mysql
```

### Change the stop timeout:
```bash
docker restart --time 30 my-mysql
docker restart -t 30 my-mysql
```

### `docker restart` vs `docker stop` + `docker start`:
| | `docker restart` | `docker stop` + `docker start` |
|-|-----------------|-------------------------------|
| Number of commands | 1 | 2 |
| Container ID | Same | Same |
| Data inside | Preserved | Preserved |
| Use case | Quick cycle | When you want a pause in between |

> 💡 **Restart policies** — set automatic restart behaviour when a container crashes:
> ```bash
> docker run --restart always my-app         # always restart, even after daemon restart
> docker run --restart on-failure my-app     # restart only if it crashes (non-zero exit)
> docker run --restart unless-stopped my-app # restart unless manually stopped
> ```

---

## 20. 🗑️ `docker rm` — Remove Containers

```bash
# Remove a single container
docker rm <Container_ID>
docker rm <Container_Name>

# Remove multiple containers at once
docker rm <Container_ID1> <Container_ID2> <Container_ID3>
```

### What it does:
- **Permanently deletes** one or more stopped containers.
- Frees up disk space and cleans up the container's writable layer.
- The **image is NOT deleted** — only the container instance.
- Cannot remove a running container without the force flag.

### Example:
```bash
docker rm my-old-mysql
docker rm a1b2c3d4e5f6 f6e5d4c3b2a1 9z8y7x6w5v4u
```

### Useful variations:
```bash
# Force-remove a running container (skips graceful stop)
docker rm -f my-mysql
docker rm --force my-mysql

# Remove all stopped containers in one go
docker container prune

# Remove a container as soon as it stops (auto-cleanup)
docker run --rm nginx
# The --rm flag on docker run auto-deletes the container when it exits
```

### Workflow — stop then remove:
```bash
docker stop my-mysql        # Step 1: stop it
docker rm my-mysql          # Step 2: delete it

# Or in one command using force:
docker rm -f my-mysql       # stops AND removes immediately
```

> 💡 Use `docker run --rm` for short-lived one-off containers (like running a script) so they automatically clean themselves up the moment they exit.

---

## 21. 🔌 `docker exec`

```bash
docker exec [OPTIONS] <container_name_or_id> COMMAND
```

### What it does:
- Runs a **command inside an already running container**.
- Unlike `docker run` (creates new container), `docker exec` enters an **existing running** one.
- The container **must already be running** — `docker exec` will not start a stopped container.

### Most common use — open interactive shell in running container:
```bash
docker exec -it my-mysql /bin/bash
docker exec -it my-mysql /bin/sh     # for Alpine images
```

### Run a single command without entering shell:
```bash
docker exec my-app java --version           # check Java version
docker exec my-app ls /app                  # list files
docker exec my-app ps aux                   # running processes
docker exec my-app env                      # all env variables
```

### `docker exec -it` vs `docker run -it`:
| | `docker run -it` | `docker exec -it` |
|-|-----------------|------------------|
| Creates new container? | ✅ Yes | ❌ No |
| Container must be running? | No | ✅ Yes |
| Use case | Explore an image fresh | Debug running container |
| Exiting shell | Stops the container | Container keeps running |

> 💡 **Golden rule:** `docker run -it` to explore an image. `docker exec -it` to debug a container already running.

---

## 22. 🔍 `docker inspect`

```bash
docker inspect <container_name_or_id>
docker inspect <image_name>
```

### What it does:
- Returns **detailed low-level JSON metadata** about a container or image.
- Shows everything Docker knows — config, networking, mounts, env vars, state, and more.
- Useful for debugging networking issues, verifying env vars, finding volumes.

### Example output (trimmed):
```json
[
  {
    "Id": "a1b2c3d4e5f6...",
    "Name": "/my-mysql",
    "State": {
      "Status": "running",
      "Running": true,
      "ExitCode": 0
    },
    "NetworkSettings": {
      "IPAddress": "172.17.0.2",
      "Ports": {
        "3306/tcp": [{ "HostPort": "3306" }]
      }
    },
    "Config": {
      "Env": ["MYSQL_ROOT_PASSWORD=secret", "MYSQL_DATABASE=mydb"],
      "Image": "mysql:8.0"
    }
  }
]
```

### Extract specific fields using `--format`:
```bash
# Get container IP address
docker inspect --format '{{.NetworkSettings.IPAddress}}' my-mysql

# Get container status
docker inspect --format '{{.State.Status}}' my-mysql

# Get all environment variables
docker inspect --format '{{.Config.Env}}' my-mysql
```

---

## 23. 📜 `docker logs`

```bash
docker logs <Container_Name>
docker logs <Container_ID>
```

### What it does:
- **Fetches and prints all logs** (stdout and stderr) from a container.
- Works for both running and stopped containers — great for debugging a container that crashed.
- Logs are everything the app printed to its standard output inside the container.

### Example:
```bash
docker logs my-spring-app
docker logs my-mysql
```

### Most useful variations:
```bash
# Follow logs in real time (like tail -f)
docker logs -f my-spring-app
docker logs --follow my-spring-app

# Show only the last N lines
docker logs --tail 50 my-spring-app

# Follow AND start from last 50 lines (most common in practice)
docker logs -f --tail 50 my-spring-app

# Show logs with timestamps
docker logs --timestamps my-spring-app
docker logs -t my-spring-app

# Show logs since a specific time
docker logs --since 30m my-spring-app    # last 30 minutes
docker logs --since 1h my-spring-app     # last 1 hour
```

### Debugging a crashed container:
```bash
# Container shows "Exited (1)" in docker ps -a?
# Read its logs to find out why it crashed
docker logs --tail 100 my-spring-app
```

> 💡 `docker logs -f --tail 50` is the command you'll use most in real development — it gives you a live tail of the last 50 lines, just like watching a log file with `tail -f`.

---

## 24. 📊 `docker stats` — Live Resource Usage

```bash
docker stats
docker stats <Container_Name>
```

### What it does:
- Shows **real-time CPU, memory, network, and disk usage** for running containers.
- Like `top` or `htop` but for Docker containers.

### Example output:
```
CONTAINER ID   NAME             CPU %   MEM USAGE / LIMIT     NET I/O
a1b2c3d4e5f6   my-mysql         0.5%    256MiB / 16GiB        1.2MB / 3.4MB
f6e5d4c3b2a1   my-spring-app    2.3%    512MiB / 16GiB        5.6MB / 2.1MB
```

```bash
# Stats for all containers, then exit (non-streaming)
docker stats --no-stream

# Stats for a specific container
docker stats my-spring-app
```

---

## 25. 📋 `docker cp` — Copy Files

```bash
# Copy FROM container TO your host machine
docker cp <Container_Name>:<path_inside_container> <path_on_host>

# Copy FROM your host machine TO container
docker cp <path_on_host> <Container_Name>:<path_inside_container>
```

### What it does:
- **Copies files or directories** between your host machine and a container.
- Works on both running and stopped containers.
- Useful for extracting logs, configs, or build artifacts from a container.

### Examples:
```bash
# Copy a log file out of a running container
docker cp my-spring-app:/app/logs/app.log ./app.log

# Copy an entire directory out
docker cp my-mysql:/var/lib/mysql/logs ./mysql-logs

# Copy a config file INTO a container
docker cp ./my-config.properties my-spring-app:/app/config/
```

### Quick reference syntax:
```
docker cp  my-container:/inside/path    ./outside/path
           ↑                            ↑
        Source                       Destination
      (container)                     (host)

docker cp  ./outside/path    my-container:/inside/path
           ↑                 ↑
        Source             Destination
        (host)             (container)
```

> ⚠️ Copying a file into a container doesn't make the change permanent — it only affects that specific container instance, not the underlying image. To bake a file into the image permanently, use `COPY` in your Dockerfile.

---
---

# 🗄️ Registry Commands

---

## 26. 🔐 `docker login`

```bash
docker login
docker login <registry_url>
```

### What it does:
- **Authenticates your Docker CLI** with Docker Hub (or another container registry).
- Required before you can `docker push` (upload) images.
- Saves credentials securely so you don't need to log in every time.
- Default registry is `docker.io` (Docker Hub) if no URL is specified.

### Example:
```bash
# Login to Docker Hub (default)
docker login
# Prompts for username and password

# Login to a private registry
docker login registry.mycompany.com

# Login with username directly (will still prompt for password)
docker login -u myusername
```

### Example session:
```
$ docker login
Username: kshitij123
Password: ••••••••••
Login Succeeded
```

> 🔒 **Security tip:** Avoid passing passwords directly in the command line (e.g., `-p mypassword`) as it gets stored in your shell history. Let Docker prompt you for the password interactively instead.

---

## 27. 📸 `docker commit`

```bash
docker commit <Container_Name_or_ID> <new_image_name>:<tag>
```

### What it does:
- **Creates a new image** from the current state of a running or stopped container.
- Captures all changes made inside the container (installed packages, modified files, etc.) into a new image layer.
- Think of it as taking a **snapshot** of a container and turning it into a reusable image.

### Example:
```bash
# Enter a container and make changes
docker run -it ubuntu:22.04 /bin/bash
root@a1b2c3d4:/# apt-get install curl     # install something
root@a1b2c3d4:/# exit

# Now commit that container as a new image
docker commit a1b2c3d4e5f6 ubuntu-with-curl:1.0

# Verify it appears in your local images
docker images
```

### Add a commit message:
```bash
docker commit -m "Added curl and git" a1b2c3d4 ubuntu-dev:1.0
```

> ⚠️ **Best Practice:** Use `docker commit` for quick experiments and debugging only. For real projects, always build images using a `Dockerfile` — it's reproducible, reviewable, and version-controllable.

---

## 28. 📤 `docker push`

```bash
docker push <dockerhub_username>/<image_name>:<tag>
```

### What it does:
- **Uploads a local image** to Docker Hub (or another registry).
- Makes your image available to others — or to your own servers and CI/CD pipelines.
- Requires you to be logged in (`docker login`) first.
- The image name must start with your Docker Hub username.

### Full workflow — build, tag, push:
```bash
# Step 1 — Build the image
docker build -t my-spring-app:1.0 .

# Step 2 — Tag it with your Docker Hub username (required for push)
docker tag my-spring-app:1.0 kshitij123/my-spring-app:1.0

# Step 3 — Push to Docker Hub
docker push kshitij123/my-spring-app:1.0
```

### Example output:
```
The push refers to repository [docker.io/kshitij123/my-spring-app]
f8d7e1c92b1a: Pushed
3218b38490ce: Pushed
latest: digest: sha256:abc123... size: 1234
```

### Push a specific tag vs all tags:
```bash
# Push one specific tag
docker push kshitij123/my-app:1.0

# Push ALL tags of an image
docker push --all-tags kshitij123/my-app
```

> 💡 After pushing, anyone can pull your image with `docker pull kshitij123/my-spring-app:1.0`. This is how you share images between your dev machine, staging server, and production.

---

## 29. 🔓 `docker logout`

```bash
docker logout
docker logout <registry_url>
```

### What it does:
- **Removes your saved credentials** for Docker Hub (or a specified registry) from your machine.
- The opposite of `docker login`.
- After logging out, `docker push` will require you to log in again.

### Example:
```bash
docker logout
docker logout registry.mycompany.com
```

### Example output:
```
Removing login credentials for https://index.docker.io/v1/
```

> 🔒 Always `docker logout` when done working on a shared machine, CI runner, or any environment where your credentials shouldn't persist.

---
---

# 🌐 Networking & Storage

---

## 30. 🌐 `docker network` — Container Networking

```bash
# List all networks
docker network ls

# Create a custom network
docker network create my-network

# Connect a container to a network
docker network connect my-network my-spring-app

# Inspect a network
docker network inspect my-network
```

### What it does:
- Docker networks let containers **communicate with each other** by name.
- By default, containers are isolated — they can't reach each other unless they're on the same network.
- When two containers share a network, they can address each other using their **container names as hostnames**.

### Why it matters — the DB_URL problem:
When you run Spring Boot and MySQL in separate containers, you can't use `localhost` in `DB_URL`. Containers have their own networking — `localhost` inside the Spring Boot container refers to the Spring Boot container itself, not MySQL.

```bash
# Create a shared network
docker network create app-network

# Run MySQL on that network
docker run -d --name my-mysql --network app-network \
  -e MYSQL_ROOT_PASSWORD=secret mysql:8.0

# Run Spring Boot on the same network — can reach MySQL by its container NAME
docker run -d --name my-spring-app --network app-network \
  -e DB_URL=jdbc:mysql://my-mysql:3306/myhiber \   # ← "my-mysql" resolves as hostname!
  my-spring-app:1.0
```

> 💡 When two containers are on the same Docker network, they can reach each other using their **container names as hostnames**. This is the correct way to connect containers together.

---

## 31. 💾 `docker volume` — Persistent Storage

```bash
# Create a named volume
docker volume create my-data

# Use a volume in a container
docker run -v my-data:/var/lib/mysql mysql:8.0

# List all volumes
docker volume ls

# Inspect a volume
docker volume inspect my-data

# Remove unused volumes
docker volume prune
```

### Why volumes matter:
By default, all data inside a container is **lost when the container is deleted**. Volumes solve this — they store data outside the container so it survives `docker rm`.

```bash
# ❌ Without a volume — delete container = lose all MySQL data
docker run -d --name my-mysql mysql:8.0
docker rm my-mysql    # all database data gone!

# ✅ With a volume — data persists even after container is removed
docker run -d --name my-mysql -v mysql-data:/var/lib/mysql mysql:8.0
docker rm my-mysql              # container gone
docker run -d --name my-mysql -v mysql-data:/var/lib/mysql mysql:8.0
# ↑ data is still there!
```

---
---

# 🧹 Cleanup

---

## 32. 🧹 `docker system prune` — Clean Up Everything

```bash
docker system prune
```

### What it does:
- **Removes all unused Docker objects** in one command: stopped containers, dangling images, unused networks, and build cache.
- Frees up significant disk space on systems that have been running Docker for a while.

```bash
# Basic prune (stopped containers + dangling images + unused networks + build cache)
docker system prune

# Also remove unused images (not just dangling ones) — be careful!
docker system prune -a

# Skip the confirmation prompt
docker system prune -f
```

> ⚠️ `docker system prune -a` removes ALL images not currently used by a running container — including images you might want to keep. Run `docker images` first to review what's there.

---
---

# 📐 Combining Flags — All Permutations

## The Master Syntax

```bash
docker run [FLAGS in any order] IMAGE_NAME[:TAG] [optional command]
```

Flags are **order-independent among themselves** — but all must come **before** the image name.

## Full Combinations Matrix

| Use Case | Command |
|----------|---------|
| Quick foreground run | `docker run nginx` |
| Background (detached) | `docker run -d nginx` |
| With custom name | `docker run --name my-app nginx` |
| Background + name | `docker run -d --name my-app nginx` |
| With port mapping | `docker run -p 8080:80 nginx` |
| Background + port | `docker run -d -p 8080:80 nginx` |
| Background + name + port | `docker run -d --name my-nginx -p 8080:80 nginx` |
| With one env var | `docker run -e KEY=VAL nginx` |
| Background + name + port + env | `docker run -d --name my-app -p 8080:80 -e KEY=VAL nginx` |
| Background + name + port + multiple envs | `docker run -d --name my-app -p 8080:80 -e K1=V1 -e K2=V2 nginx` |
| Background + name + port + env file | `docker run -d --name my-app -p 8080:80 --env-file .env nginx` |
| Interactive shell | `docker run -it nginx /bin/bash` |
| Interactive + name | `docker run -it --name debug nginx /bin/bash` |
| Auto-delete on exit | `docker run --rm nginx` |
| With network | `docker run -d --network app-network nginx` |
| With volume | `docker run -d -v my-data:/data nginx` |
| Everything combined | `docker run -d --name my-app -p 8080:80 -e KEY=VAL --network app-network -v my-data:/data nginx` |

---

# 🧠 Full Quick Reference Table

| Command | What it does |
|---------|-------------|
| `docker -v` | Check Docker version |
| `docker pull <name>` | Download latest version of image |
| `docker pull <name>:<tag>` | Download specific version of image |
| `docker images` | List all local images |
| `docker images -q` | List only image IDs |
| `docker search <name>` | Search Docker Hub for images |
| `docker build -t name:tag .` | Build image from Dockerfile |
| `docker rmi <image>` | Delete a local image |
| `docker image prune -a` | Delete all unused images |
| `docker tag old:tag new:tag` | Retag an image |
| `docker run <image>` | Create and start a new container |
| `docker run -p host:container image` | Map host port to container port |
| `docker run -d <image>` | Run container in background |
| `docker run --name <n> image` | Assign a custom name |
| `docker run -e KEY=VAL image` | Set environment variable |
| `docker run -it image /bin/bash` | Open interactive shell |
| `docker run --rm image` | Auto-delete container on exit |
| `docker run --restart always image` | Auto-restart on crash |
| `docker run --network <n> image` | Attach to a Docker network |
| `docker run -v vol:/path image` | Mount a persistent volume |
| `docker ps` | List running containers |
| `docker ps -a` | List all containers (including stopped) |
| `docker start <n>` | Start a stopped container |
| `docker stop <n>` | Gracefully stop a container |
| `docker restart <n>` | Stop and start a container |
| `docker rm <id1> <id2>` | Delete one or more containers |
| `docker rm -f <n>` | Force-stop and delete a container |
| `docker exec -it <n> /bin/bash` | Open shell in running container |
| `docker inspect <n>` | Show detailed JSON metadata |
| `docker logs <n>` | Print container logs |
| `docker logs -f --tail 50 <n>` | Follow live logs (last 50 lines) |
| `docker stats` | Live CPU/memory usage |
| `docker stats --no-stream` | Snapshot of resource usage |
| `docker cp container:/path ./path` | Copy file out of container |
| `docker cp ./path container:/path` | Copy file into container |
| `docker login` | Authenticate with Docker Hub |
| `docker logout` | Remove saved registry credentials |
| `docker commit <container> image:tag` | Create image from container state |
| `docker push username/image:tag` | Upload image to Docker Hub |
| `docker network create <n>` | Create a custom network |
| `docker network ls` | List all networks |
| `docker volume create <n>` | Create a persistent volume |
| `docker volume ls` | List all volumes |
| `docker system prune` | Clean up all unused objects |
| `docker system prune -a` | Clean up including unused images |

---

# 🔄 Complete Real-World Workflow

```bash
# ── SETUP ──────────────────────────────────────────────────

# 1. Login to Docker Hub
docker login

# 2. Create a shared network and persistent volume
docker network create app-network
docker volume create mysql-data

# ── BUILD ──────────────────────────────────────────────────

# 3. Build your Spring Boot image
docker build -t my-spring-app:1.0 .

# ── RUN ────────────────────────────────────────────────────

# 4. Start MySQL with networking + persistence
docker run -d \
  --name my-mysql \
  --network app-network \
  -p 3306:3306 \
  -v mysql-data:/var/lib/mysql \
  -e MYSQL_ROOT_PASSWORD=secret \
  -e MYSQL_DATABASE=myhiber \
  mysql:8.0

# 5. Start your Spring Boot app
docker run -d \
  --name my-spring-app \
  --network app-network \
  -p 8081:8081 \
  -e DB_URL=jdbc:mysql://my-mysql:3306/myhiber \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=secret \
  my-spring-app:1.0

# ── MONITOR ────────────────────────────────────────────────

# 6. Check both are running
docker ps

# 7. Follow Spring Boot logs live
docker logs -f --tail 50 my-spring-app

# 8. Check resource usage
docker stats --no-stream

# ── DEBUG ──────────────────────────────────────────────────

# 9. Shell into the Spring Boot container
docker exec -it my-spring-app /bin/bash

# 10. Copy a log file out
docker cp my-spring-app:/app/logs/app.log ./debug.log

# 11. Inspect networking details
docker inspect my-spring-app

# ── DEPLOY ─────────────────────────────────────────────────

# 12. Tag and push to Docker Hub
docker tag my-spring-app:1.0 kshitij123/my-spring-app:1.0
docker push kshitij123/my-spring-app:1.0

# ── CLEANUP ────────────────────────────────────────────────

# 13. Stop and remove containers
docker stop my-spring-app my-mysql
docker rm my-spring-app my-mysql

# 14. Clean up unused images and build cache
docker system prune

# 15. Logout
docker logout
```

---

# ⚠️ Common Mistakes and Fixes

| Mistake | Error | Fix |
|---------|-------|-----|
| Flags after image name | `unexpected flag` | Move all flags before image name |
| Duplicate `--name` | `name already in use` | `docker rm old-container` first |
| No `-p` flag | App runs but browser can't connect | Add `-p host:container` |
| Wrong port order (`-p 80:8080`) | Connects to wrong port | Always `-p host:container` |
| Host port already in use | `bind: address already in use` | Use a different host port |
| `docker exec` on stopped container | `is not running` | `docker start <container>` first |
| `docker rmi` while container exists | `image is being used` | `docker rm <container>` first |
| Connecting containers via `localhost` | `Connection refused` | Use Docker networks + container names |
| Data lost after `docker rm` | (silent data loss) | Use `-v` volumes for important data |
| Pushing without `docker login` | `unauthorized` | `docker login` first |
| Pushing without username prefix | `denied: requested access forbidden` | `docker tag` with `username/image:tag` |
| Shell exits immediately | no `-it` flag | Always use `-it` when opening a shell |
| Forgetting `-f` for live logs | Logs print once and stop | Use `docker logs -f my-app` |

---
---

# 📝 Concept Clarity: What is "Base OS"?

> When we say an image has a **Base OS**, do we mean the Host Machine's OS? Or the runtime like Java?
> **Neither.** Base OS, Host OS, and Runtime are three completely different things.

---

### 🖥️ Host OS
The OS **physically running on your machine** — your Windows, Ubuntu, or macOS. This is what you installed on your laptop. Docker Engine runs on top of this.

---

### 📦 Base OS (inside the image)
A **minimal Linux OS bundled inside the Docker image itself**. It is NOT your machine's OS — it's a tiny, stripped-down Linux (like Ubuntu, Debian, or Alpine) that gets packaged into every image as the bottom-most foundation layer.

Think of it like this:

```
Your Laptop (Windows / macOS)       ← Host OS
    └── Docker Engine
            └── Container
                    └── Alpine Linux (5MB!)   ← Base OS (inside the image)
                            └── Java 17       ← Runtime
                                    └── Your Spring Boot App
```

---

### 🤔 Why Does the Image Need Its Own OS Inside It?

Because containers need a **consistent Linux foundation** to run on — regardless of what the Host OS is.

- If you're on **Windows** and your teammate is on **macOS**, the container still behaves identically because it carries its own Base OS inside it.
- The Base OS provides basic tools like file system structure, package managers (`apt`, `apk`), and system libraries that your runtime (Java, Python, etc.) depends on.

---

### 🆚 Host OS vs Base OS vs Runtime — Side by Side

| | Host OS | Base OS | Runtime |
|-|---------|---------|---------|
| **What** | Your machine's OS | Tiny Linux inside the image | Java, Python, Node.js |
| **Where** | Physical machine | Inside Docker image (bottom layer) | Inside Docker image, on top of Base OS |
| **Example** | Windows 11, macOS | Alpine Linux, Debian slim | JDK 17, Python 3.10 |
| **Size** | Full OS (GBs) | 5MB – 50MB | 100MB – 400MB |
| **Who installs it** | You | Comes with base image (`FROM`) | You specify in `FROM` or `RUN` |
| **Shared?** | Shared by Docker Engine | Bundled per image | Bundled per image |

---

### 🔍 Connecting It Back to Your Dockerfile

```dockerfile
FROM openjdk:17-jdk-slim
```

This one line pulls **two things bundled together**:
- A **slim Debian Linux** (the Base OS) → ~30MB
- **JDK 17** installed on top of it (the Runtime) → ~380MB

If you wanted to be more explicit, you could split it into two steps:

```dockerfile
FROM debian:slim           # ← just the Base OS
RUN apt-get install java   # ← then manually install the Runtime on top
```

But `FROM openjdk:17-jdk-slim` is a **pre-built image** that already did those steps for you — which is why it's called a **base image**. You're building your app on top of someone else's already-prepared foundation.

---

### 🧅 Full Image Layer Stack

```
  [ ENTRYPOINT java -jar app.jar  ]  ← startup command
                 │
  [ app.jar (your Spring Boot JAR)]  ← COPY
                 │
  [ WORKDIR /app                  ]  ← directory setup
                 │
  [ JDK 17 (~380MB)               ]  ← Runtime
                 │
  [ Debian Slim Linux (~30MB)     ]  ← Base OS
```

> 💡 When you run `docker pull openjdk:17-jdk-slim`, you are downloading both the Base OS (Debian slim) and the Runtime (JDK 17) together as pre-built layers. Your `COPY` and `ENTRYPOINT` instructions then add your own layers on top.