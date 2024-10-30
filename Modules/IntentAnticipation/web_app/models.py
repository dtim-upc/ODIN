from flask_login import UserMixin
from . import db
from datetime import datetime, timezone


class User(UserMixin, db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(100), unique=True)
    password = db.Column(db.String(100))
    name = db.Column(db.String(1000))

class Dataset(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    email = db.Column(db.String(120), nullable=False)
    dataset_name = db.Column(db.String(120), nullable=False)
    file_hash = db.Column(db.String(64), nullable=False)  # SHA-256 hash size

class Intent(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    timestamp = db.Column(db.DateTime, default=lambda: datetime.now(timezone.utc), nullable=False)
    email = db.Column(db.String(100), nullable=False)
    dataset_name = db.Column(db.String(1000), nullable=False)
    problem_description = db.Column(db.String(2000), nullable=False)
    model = db.Column(db.String(1000), nullable=False)
    intent = db.Column(db.String(500), nullable=False)
