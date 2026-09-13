FROM python:3.11-slim
WORKDIR /app

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY ai_hardware_simulator.py .

CMD ["python", "ai_hardware_simulator.py"]