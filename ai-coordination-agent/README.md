# EventSync AI Coordination Agent

Ready-to-run Python FastAPI microservice for the EventSync project.

## What it does

- Intelligent volunteer allocation
- Skill matching
- Availability filtering
- Experience scoring
- Workload balancing
- Dynamic reassignment

## Project structure

```text
ai-coordination-agent/
├── main.py
├── agent.py
├── models.py
├── requirements.txt
├── sample_assign.json
├── sample_reassign.json
├── run_mac.sh
└── README.md
```

## Run directly in VS Code on macOS

Open this folder in VS Code, then open Terminal:

```bash
chmod +x run_mac.sh
./run_mac.sh
```

Or run manually:

```bash
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --host 127.0.0.1 --port 8005
```

## Open

Root:
http://127.0.0.1:8005

Swagger UI:
http://127.0.0.1:8005/docs

Health:
http://127.0.0.1:8005/health

## Test assignment

In Swagger, open `POST /api/agent/assign`, click **Try it out**, and paste the contents of `sample_assign.json`.

## Test reassignment

Open `POST /api/agent/reassign` and paste the contents of `sample_reassign.json`.

## Scoring

Final score:

- Skills: 40%
- Availability: 20%
- Experience: 20%
- Workload capacity: 20%

The service is deterministic and explainable, making it suitable for the current Review-2 implementation. It can later be connected to the Spring Boot services through REST APIs.
