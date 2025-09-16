``` mermaid
---
config:
  layout: elk
  theme: redux
  look: neo
---

flowchart TB
subgraph Public_Subnet["Public Subnet (10.0.1.0/24)"]
EC2Public["Public EC2<br>(t2.micro)<br><b>+ Nginx Reverse Proxy</b>"]
end
subgraph Private_Subnet["Private Subnet (10.0.2.0/24)"]
SpringApp["Spring App<br>on Docker"]
Postgre["PostgreSQL + PostGIS"]
Redis["Redis"]
end
subgraph VPC_main["VPC: main-vpc (10.0.0.0/16)"]
IGW["인터넷 게이트웨이<br>(main-igw)"]
RT["라우팅 테이블<br>(public-rt)"]
Public_Subnet
Private_Subnet
SG["보안 그룹: ssh-sg<br>(22포트 허용)"]
Internet["🌐 Internet"]
end
subgraph TechStack["기술 스택"]
Spring["Spring Boot"]
ReactNative["React Native"]
Nginx["Nginx"]
Docker["Docker"]
PostgreSQL["PostgreSQL + PostGIS"]
RedisStack["Redis"]
Prometheus["Prometheus"]
Loki["Loki"]
Grafana["Grafana"]
end
IGW -.-> RT
EC2Public --> SG & Nginx
SpringApp --> SG & Postgre & Redis & Spring & Docker
Public_Subnet --> RT
RT --> IGW
Internet --> IGW
EC2Public -- Proxy --> SpringApp
Dev["👩‍💻 React Native App"] --> EC2Public
Monitoring["🛠️ Grafana + Prometheus + Loki"] --> EC2Public & Prometheus & Loki & Grafana
Postgre --> PostgreSQL
Redis --> RedisStack

```