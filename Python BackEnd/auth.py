from functools import wraps
from flask import Blueprint, request, jsonify, current_app, g
from itsdangerous import URLSafeTimedSerializer, BadSignature, SignatureExpired
from werkzeug.security import generate_password_hash, check_password_hash
from db import db
from models import Usuario

auth_bp = Blueprint("auth", __name__, url_prefix="/auth")

# 7 dias por enquanto, porque o app ainda não usa o refresh_token
ACCESS_MAX_AGE = 60 * 60 * 24 * 7


def _serializer():
    return URLSafeTimedSerializer(current_app.config["SECRET_KEY"])


def _resposta_auth(u):
    s = _serializer()
    return {
        "access_token": s.dumps({"uid": u.id, "tipo": "access"}),
        "refresh_token": s.dumps({"uid": u.id, "tipo": "refresh"}),
        "user": {"id": u.id, "name": u.nome, "email": u.email},
    }


def token_required(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        header = request.headers.get("Authorization", "")
        if not header.startswith("Bearer "):
            return jsonify({"erro": "Token ausente"}), 401

        token = header[7:].strip()
        try:
            dados = _serializer().loads(token, max_age=ACCESS_MAX_AGE)
        except SignatureExpired:
            return jsonify({"erro": "Token expirado"}), 401
        except BadSignature:
            return jsonify({"erro": "Token inválido"}), 401

        if dados.get("tipo") != "access":
            return jsonify({"erro": "Token inválido"}), 401

        u = db.session.get(Usuario, dados.get("uid"))
        if not u:
            return jsonify({"erro": "Usuário não encontrado"}), 401

        g.usuario = u
        return f(*args, **kwargs)
    return wrapper


@auth_bp.route("/register", methods=["POST"])
def register():
    dados = request.get_json(silent=True) or {}
    nome = (dados.get("name") or "").strip()
    email = (dados.get("email") or "").strip().lower()
    senha = dados.get("password") or ""

    if not nome or not email or not senha:
        return jsonify({"erro": "Dados inválidos"}), 400

    if Usuario.query.filter_by(email=email).first():
        return jsonify({"erro": "E-mail já cadastrado"}), 409

    u = Usuario(nome=nome, email=email, senha=generate_password_hash(senha))
    db.session.add(u)
    db.session.commit()
    return jsonify(_resposta_auth(u)), 201


@auth_bp.route("/login", methods=["POST"])
def login():
    dados = request.get_json(silent=True) or {}
    email = (dados.get("email") or "").strip().lower()
    senha = dados.get("password") or ""

    u = Usuario.query.filter_by(email=email).first()
    if not u or not check_password_hash(u.senha, senha):
        return jsonify({"erro": "E-mail ou senha incorretos"}), 401

    return jsonify(_resposta_auth(u)), 200


@auth_bp.route("/me", methods=["GET"])
@token_required
def me():
    u = g.usuario
    return jsonify({"id": u.id, "name": u.nome, "email": u.email}), 200