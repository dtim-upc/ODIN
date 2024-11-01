import requests
from openai import OpenAI
from llamaapi import LlamaAPI
from dotenv import load_dotenv
import os

def load_api_key(api_name):
    """
    Retrieves the API key for a given service from environment variables.
    """
    try:
        load_dotenv()
        return os.getenv(f"{api_name}_KEY")
    except Exception as e:
        raise Exception(f"Error loading API key: {str(e)}")

def call_api(api, content, model):
    try:
        api_request_json = {
            "model": model,
            "messages": [
                {"role": "user", "content": content},
            ]
        }
        response = api.run(api_request_json)
        response_data = response.json()
        return response_data
    except Exception as e:
        raise Exception(f"Error calling API: {str(e)}")

def extract_label(predicted_label, labels):
    """
    Extracts the first matching label based on the predicted label.
    """
    try:
        for label in labels:
            if label in predicted_label:
                return label
        return 'unknown'
    except Exception as e:
        raise Exception(f"Error extracting label: {str(e)}")

def get_prediction_llama_mistral(api_key, content, labels, model):
    """
    Retrieves and extracts a label from the prediction response of the LlamaAPI based on the given content and model.
    """
    try:
        llama = LlamaAPI(api_key)
        response_data = call_api(llama, content, model)

        predicted_label = None
        for choice in response_data['choices']:
            if choice['message']['role'] == 'assistant':
                predicted_label = choice['message']['content']
                break

        return extract_label(predicted_label, labels)
    except Exception as e:
        raise Exception(f"Error processing Llama/Mistral prediction: {str(e)}")

def get_prediction_gpt(content, labels, model):
    """
    Retrieves and extracts a label from the GPT model's prediction based on the provided content and model.
    """
    try:
        client = OpenAI()
        completion = client.chat.completions.create(
            model=model,
            messages=[{"role": "user", "content": content}]
        )
        prediction = completion.choices[0].message.content.lower()
        return extract_label(prediction, labels)
    except Exception as e:
        raise Exception(f"Error processing GPT prediction: {str(e)}")

def get_prediction(text_data, selected_model):
    """
    Classifies the provided text using the specified model and returns the predicted label.
    """
    try:
        url = f"{os.getenv('INTENTS2WORKFLOWS_URL')}/problems"  # get the labels from the ontology
        response = requests.get(url)
        labels = list(response.json().keys())

        # labels = [
        #     'data profiling', 'classification', 'correlation',
        #     'anomaly detection', 'clustering', 'causal inference',
        #     'association rules', 'regression', 'forecasting'
        # ]

        # content = f"Classes: {labels}\nText: {text_data}\n\nClassify the text into one of the above classes."

        # if "llama" in selected_model or "mixtral" in selected_model or "mistral" in selected_model:
        #     api_key = load_api_key("LlamaAPI")
        #     prediction = get_prediction_llama_mistral(api_key, content, labels, model=selected_model)
        # elif "gpt" in selected_model:
        #     prediction = get_prediction_gpt(content, labels, model=selected_model)
        # else:
        #     raise ValueError("Invalid model selected")

        ## To integrate, we will not generate the intent prediction, we just return "Classification"
        prediction = "Classification"

        return prediction
    except Exception as e:
        raise Exception(f"Error getting prediction: {str(e)}")
