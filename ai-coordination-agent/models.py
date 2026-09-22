from pydantic import BaseModel
from typing import List


class Volunteer(BaseModel):
    id: int
    name: str
    skills: List[str]
    availability: bool
    experience: int
    current_workload: int
    max_workload: int


class Task(BaseModel):
    id: int
    name: str
    required_skills: List[str]
    required_experience: int
    workload: int


class AssignmentRequest(BaseModel):
    task: Task
    volunteers: List[Volunteer]


class ReassignmentRequest(BaseModel):
    task: Task
    unavailable_volunteer_id: int
    volunteers: List[Volunteer]
