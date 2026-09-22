class AICoordinationAgent:
    def __init__(self):
        self.skill_weight = 40
        self.availability_weight = 20
        self.experience_weight = 20
        self.workload_weight = 20

    def calculate_skill_score(self, volunteer, task):
        if not task.required_skills:
            return 100

        volunteer_skills = {skill.lower() for skill in volunteer.skills}
        matched = sum(
            1 for skill in task.required_skills
            if skill.lower() in volunteer_skills
        )
        return (matched / len(task.required_skills)) * 100

    def calculate_availability_score(self, volunteer):
        return 100 if volunteer.availability else 0

    def calculate_experience_score(self, volunteer, task):
        if task.required_experience <= 0:
            return 100

        score = (volunteer.experience / task.required_experience) * 100
        return min(score, 100)

    def calculate_workload_score(self, volunteer):
        if volunteer.max_workload <= 0:
            return 0

        remaining = volunteer.max_workload - volunteer.current_workload
        if remaining <= 0:
            return 0

        return (remaining / volunteer.max_workload) * 100

    def calculate_final_score(self, volunteer, task):
        skill = self.calculate_skill_score(volunteer, task)
        availability = self.calculate_availability_score(volunteer)
        experience = self.calculate_experience_score(volunteer, task)
        workload = self.calculate_workload_score(volunteer)

        final_score = (
            skill * self.skill_weight / 100
            + availability * self.availability_weight / 100
            + experience * self.experience_weight / 100
            + workload * self.workload_weight / 100
        )
        return round(final_score, 2)

    def rank_volunteers(self, volunteers, task):
        ranked = []

        for volunteer in volunteers:
            if not volunteer.availability:
                continue

            if volunteer.current_workload + task.workload > volunteer.max_workload:
                continue

            ranked.append({
                "volunteer_id": volunteer.id,
                "volunteer_name": volunteer.name,
                "score": self.calculate_final_score(volunteer, task),
                "skill_score": round(
                    self.calculate_skill_score(volunteer, task), 2
                ),
                "experience_score": round(
                    self.calculate_experience_score(volunteer, task), 2
                ),
                "workload_score": round(
                    self.calculate_workload_score(volunteer), 2
                )
            })

        ranked.sort(key=lambda x: x["score"], reverse=True)
        return ranked

    def assign_volunteer(self, volunteers, task):
        ranked = self.rank_volunteers(volunteers, task)

        if not ranked:
            return {
                "success": False,
                "message": "No suitable volunteer found",
                "assignment": None,
                "alternatives": []
            }

        return {
            "success": True,
            "message": "Volunteer assigned successfully",
            "assignment": ranked[0],
            "alternatives": ranked[1:3]
        }

    def reassign_volunteer(self, volunteers, task, unavailable_volunteer_id):
        available = [
            volunteer for volunteer in volunteers
            if volunteer.id != unavailable_volunteer_id
        ]

        result = self.assign_volunteer(available, task)

        if result["success"]:
            result["message"] = (
                "Volunteer unavailable. Replacement selected successfully."
            )

        return result
