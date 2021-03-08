# Install dependency phase
#FROM python:3.8 AS builder
#COPY requirements.txt .

# install dependencies to the local user directory (eg. /root/.local)
#RUN pip install --user -r requirements.txt

# Run | Production phase
FROM python:3.6
WORKDIR /code

# copy only the dependencies installation from the 1st stage image
# COPY --from=builder /root/.local/bin /root/.local
COPY . .

RUN pip3 install -r requirements.txt

# update PATH environment variable
# ENV KAFKA_CONTAINER=kafka

CMD [ "python3", "-u", "./src/main.py" ]
