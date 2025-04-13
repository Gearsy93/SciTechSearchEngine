from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
from FlagEmbedding import BGEM3FlagModel
from numpy.linalg import norm
import torch
import os

resources_path = os.path.dirname(os.path.dirname(os.getcwd()))
safetensors_path = os.path.join(resources_path, "resources", "model", "checkpoint", "safetensors")

app = FastAPI()
model = BGEM3FlagModel(
    safetensors_path.replace("\\", "//"),
    use_fp16=True,
    device="cuda"
)

class EmbeddingRequest(BaseModel):
    term: str
    context: List[str]
    title: str

def normalize_vector(v):
    n = norm(v)
    return v / n if n != 0 else v

@app.post("/embedding")
def generate_embedding(data: EmbeddingRequest):
    full_input = (
        f"{data.term} — ключевой термин в предметной области {data.title}. "
        f"{data.title} — это область, в которой рассматривается контекст: {', '.join(data.context)}. "
        f"Термин '{data.term}' используется преимущественно в {data.title}."
    )

    embedding_result = model.encode([full_input], batch_size=256)
    embedding = embedding_result["dense_vecs"][0]
    embedding = normalize_vector(embedding).tolist()

    torch.cuda.empty_cache()

    return {"embedding": embedding}

@app.post("/embedding/batch")
def generate_embeddings(data: List[EmbeddingRequest]):
    inputs = [
        f"{d.term} — ключевой термин в предметной области {d.title}. "
        f"{d.title} — это область, в которой рассматривается контекст: {', '.join(d.context)}. "
        f"Термин '{d.term}' используется преимущественно в {d.title}."
        for d in data
    ]

    embedding_result = model.encode(inputs, batch_size=256)
    vectors = embedding_result["dense_vecs"]

    torch.cuda.empty_cache()

    return [{"embedding": normalize_vector(vec).tolist()} for vec in vectors]

@app.post("/embedding/rubric")
def generate_rubric_embedding(data: dict):
    """
    data = {
        "title": "Математическая кибернетика",
        "terms": ["математическая кибернетика", "управление", "информационные процессы"]
    }
    """
    max_terms = 800
    terms = data.get("terms", [])[:max_terms]
    title = data.get("title", "")
    sentence = ", ".join(terms + [title, title])

    embedding_result = model.encode([sentence], batch_size=1)
    embedding = normalize_vector(embedding_result["dense_vecs"][0]).tolist()

    return {"embedding": embedding}
