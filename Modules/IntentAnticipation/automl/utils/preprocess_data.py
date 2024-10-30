from sklearn.preprocessing import LabelEncoder
from sklearn.preprocessing import StandardScaler
import os
import pandas as pd
import numpy as np
import json


def preprocess_dataset(data_file_path):
    """
    Loads, preprocesses the dataset by encoding categorical variables and scaling features, then saves the processed data to a new CSV file.
    """
    file_name = os.path.basename(data_file_path)     
    df = pd.read_csv(data_file_path)
    df.rename(columns={df.columns[-1]: 'target'}, inplace=True)
    
    categorical_columns = df.select_dtypes(include=['object']).columns
    for column in categorical_columns:
            df[column] = LabelEncoder().fit_transform(df[column])

    X = df.iloc[:, :-1]
    y = df.iloc[:, -1]
    StandardScalerModel = StandardScaler()
    X = StandardScalerModel.fit_transform(X)

    data = np.column_stack((X, y))
    column_names = df.columns
    res = pd.DataFrame(data,columns =column_names)
    for column in res.columns:
        res[column] = pd.to_numeric(res[column])
    
    preprocessed_file_path = f'data/preprocessed/{file_name}'
    if not os.path.exists('data/preprocessed'):
            os.makedirs('data/preprocessed')

    res.to_csv(preprocessed_file_path, index=False)
    return X, y


def preprocess_json(json_file_path):
    """
    Loads and modifies a JSON configuration file by updating dataset paths and intent values(lower case), then saves the updated configuration to a new JSON file.
    """
    file_name = "working_request.json"

    with open(json_file_path, 'r') as file:
        data = json.load(file)
    
    data['dataset'] = 'data/preprocessed/' +  data['dataset']
    data['intent'] = data['intent'].lower()
    data['preprocessing'] = True if data['preprocessing'] == 'Yes' else False
    
    # Write the modified data to a new JSON file
    preprocessed_file_path = f'data/preprocessed/{file_name}'

    with open(preprocessed_file_path, 'w') as file:
        json.dump(data, file, indent=4)