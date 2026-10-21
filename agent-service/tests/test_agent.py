from app.main import AgentRequest, task_instruction


def test_supported_agent_tasks_have_distinct_instructions():
    assert "study summary" in task_instruction("summarize").lower()
    assert "5 useful study questions" in task_instruction("quiz").lower()
    assert "source-grounded research assistant" in task_instruction("research").lower()
    assert "only the supplied excerpts" in task_instruction("ask").lower()


def test_agent_request_accepts_supported_task():
    request = AgentRequest(task="quiz", question="Quiz me on normalization", ownerEmail="student@example.com")
    assert request.task == "quiz"
    assert request.ownerEmail == "student@example.com"
