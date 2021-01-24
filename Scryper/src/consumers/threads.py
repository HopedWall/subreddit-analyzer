from kafka import KafkaConsumer
import json

# see https://towardsdatascience.com/kafka-python-explained-in-10-lines-of-code-800e3e07dad1
# for understanding the parameters
# consumer = KafkaConsumer(
#     'threads',
#      bootstrap_servers=['localhost:9092'],
#      auto_offset_reset='earliest',
#      enable_auto_commit=True,
#      group_id='my-group',
#      value_deserializer=lambda x: json.load(x.decode('utf-8')))


#consumer = KafkaConsumer('threads',bootstrap_servers='localhost:9092', key_deserializer=lambda k: k.decode('utf-8'), value_deserializer=lambda x: x.decode('utf-8'))

# for part in consumer.partitions_for_topic('threads'):
# 	print(part)

#for msg in consumer:
#	print(msg.key)
#	print(json.loads(msg.value))

# consumer = KafkaConsumer('users',bootstrap_servers='localhost:9092', key_deserializer=lambda k: k.decode('utf-8'), value_deserializer=lambda x: x.decode('utf-8'))

# for msg in consumer:
# 	print(msg.key)
# 	print(json.loads(msg.value))

consumer = KafkaConsumer('threads', bootstrap_servers='localhost:9092', key_deserializer=lambda k: k.decode('utf-8'), value_deserializer=lambda x: x.decode('utf-8'))

for msg in consumer:
	print(msg)
	print(msg.key)
	print(json.loads(msg.value))
	#sleep(100)