from fastapi import FastAPI
from models import AssignmentRequest, ReassignmentRequest
from agent import AICoordinationAgent

app = FastAPI(
    title="EventSync AI Coordination Agent",
    description=(
        "AI-based volunteer allocation, dynamic reassignment "
        "and workload balancing"
    ),
    version="1.0.0"
)

agent = AICoordinationAgent()


@app.get("/")
def root():
    return {
        "service": "EventSync AI Coordination Agent",
        "status": "running",
        "features": [
            "Intelligent Allocation",
            "Dynamic Reassignment",
            "Workload Balancing"
        ]
    }


@app.get("/health")
def health():
    return {"status": "UP"}


@app.post("/api/agent/assign")
def assign_volunteer(request: AssignmentRequest):
    return agent.assign_volunteer(
        request.volunteers,
        request.task
    )


@app.post("/api/agent/reassign")
def reassign_volunteer(request: ReassignmentRequest):
    return agent.reassign_volunteer(
        request.volunteers,
        request.task,
        request.unavailable_volunteer_id
    )
