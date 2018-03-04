import pandas as pd
import numpy as np
import os 

HEADER = ['taxi_id', 'date_time', 'longitude', 'latitude']

def get_data_from_folder(path):
	global HEADER 
	return pd.read_csv(path, infer_datetime_format = True, header = None, parse_dates = [1], names = HEADER)

file = input('Insert file name: ')

data = get_data_from_folder(file)

data.to_csv('taxi_data.csv', index=False)
