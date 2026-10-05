FROM python:3.12-slim
WORKDIR /demo
RUN pip install --no-cache-dir psycopg2-binary==2.9.10 redis==5.2.1
COPY demo.py test_demo.py /demo/
ENV PYTHONUNBUFFERED=1
ENTRYPOINT ["python", "/demo/demo.py"]
