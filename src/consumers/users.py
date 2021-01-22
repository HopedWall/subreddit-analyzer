from kafka import KafkaConsumer
import json

consumer = KafkaConsumer('users',bootstrap_servers='localhost:9092', key_deserializer=lambda k: k.decode('utf-8'), value_deserializer=lambda x: x.decode('utf-8'))

for msg in consumer:
	print(msg.key)
	print(json.loads(msg.value))