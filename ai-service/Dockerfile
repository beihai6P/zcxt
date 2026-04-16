FROM python:3.14-slim
WORKDIR /app
COPY ai-service/requirements.txt /app/requirements.txt
RUN pip install --no-cache-dir -r /app/requirements.txt
COPY ai-service /app
EXPOSE 9000
CMD ["uvicorn","main:app","--host","0.0.0.0","--port","9000"]

