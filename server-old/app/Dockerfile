FROM python:3.6

EXPOSE 5000


COPY ./requirements.txt /app/requirements.txt
WORKDIR app
RUN pip install -r requirements.txt

COPY . /app


CMD python bsq-server.py --logging=info
