# 🐳 docker-learning

A structured, hands-on learning journal documenting my journey through Docker — from core concepts to real-world containerization.

---

## 📌 About This Repo

This repository contains all my notes, commands, Dockerfiles, configurations, and mini-projects created while learning Docker. Each folder corresponds to a concept or topic, making it easy to navigate and revise.

> 📺 Learning resource: [Docker Playlist on YouTube](https://youtube.com/playlist?list=PL0zysOflRCekdY4189QaG0YkxJ6yDaP1F)

---

## 🗂️ Repo Structure

```
docker-learning/
│
├── 01-intro/
│   ├── notes.md               # What is Docker, why use it
│   └── commands.md            # Basic Docker CLI commands
│
├── 02-images/
│   ├── notes.md               # Docker images, layers, registries
│   ├── Dockerfile             # First custom image
│   └── commands.md            # pull, build, tag, push
│
├── 03-containers/
│   ├── notes.md               # Container lifecycle
│   └── commands.md            # run, stop, start, rm, exec, logs
│
├── 04-volumes/
│   ├── notes.md               # Persistent storage in Docker
│   └── commands.md            # volume create, mount, bind mounts
│
├── 05-networking/
│   ├── notes.md               # Docker networks, bridge, host, overlay
│   └── commands.md            # network create, inspect, connect
│
├── 06-docker-compose/
│   ├── notes.md               # Multi-container apps with Compose
│   ├── docker-compose.yml     # Sample compose file
│   └── commands.md            # up, down, build, logs
│
├── 07-dockerizing-springboot/
│   ├── notes.md               # Containerizing a Spring Boot app
│   ├── Dockerfile             # Dockerfile for Spring Boot
│   └── docker-compose.yml     # Spring Boot + MySQL setup
│
└── README.md
```

---

## 🧠 Topics Covered

| # | Topic | Status |
|---|-------|--------|
| 1 | Introduction to Docker & Containers | ⬜ Pending |
| 2 | Docker Images & Dockerfile | ⬜ Pending |
| 3 | Docker Containers & Lifecycle | ⬜ Pending |
| 4 | Volumes & Persistent Storage | ⬜ Pending |
| 5 | Docker Networking | ⬜ Pending |
| 6 | Docker Compose | ⬜ Pending |
| 7 | Dockerizing a Spring Boot App | ⬜ Pending |

> Update status to ✅ Done / 🔄 In Progress as you go!

---

## ⚡ Quick Command Reference

```bash
# Pull an image
docker pull <image-name>

# Run a container
docker run -d -p 8080:8080 --name my-container <image-name>

# List running containers
docker ps

# List all containers
docker ps -a

# Stop a container
docker stop <container-id>

# Remove a container
docker rm <container-id>

# Build an image from Dockerfile
docker build -t my-image:1.0 .

# View logs
docker logs <container-id>

# Open a shell inside a container
docker exec -it <container-id> /bin/bash
```

---

## 🛠️ Tools & Setup

- **Docker Desktop** — [Download here](https://www.docker.com/products/docker-desktop/)
- **Docker CLI** — comes with Docker Desktop
- **Docker Hub** — [hub.docker.com](https://hub.docker.com) (for pulling/pushing images)
- **VS Code** — with the Docker extension for easy management

---

## 🔗 Resources

- 📺 [YouTube Playlist](https://youtube.com/playlist?list=PL0zysOflRCekdY4189QaG0YkxJ6yDaP1F)
- 📘 [Official Docker Docs](https://docs.docker.com)
- 🐳 [Docker Hub](https://hub.docker.com)
- 📄 [Dockerfile Reference](https://docs.docker.com/engine/reference/builder/)
- 🧩 [Docker Compose Reference](https://docs.docker.com/compose/)

---

## 👨‍💻 Author

**Kshitij Yadav**
- GitHub: [@YadavKsh](https://github.com/YadavKsh)
- Learning Docker as part of backend development journey with Java & Spring Boot
