from flask import Flask
from db import db
from auth import auth_bp
import os
from analise import analise_bp


app.config['SECRET_KEY'] = os.environ["SECRET_KEY"]

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///usuarios.db'
db.init_app(app)

app.register_blueprint(auth_bp)
app.register_blueprint(analise_bp)
if __name__ == '__main__':
    with app.app_context():
        db.create_all()
    app.run(host="0.0.0.0", port=5000, debug=True)

    from flask import Blueprint, request, jsonify
from auth import token_required

analise_bp = Blueprint("analise", __name__, url_prefix="/api")


def analisar_texto(app_origem, texto):
    """
    TODO: implementar a análise real.
    Deve devolver um dict: {"suspeito": bool, "confianca": float, "motivos": [str]}
    """
    raise NotImplementedError


@analise_bp.route("/analisar", methods=["POST"])
@token_required
def analisar():
    dados = request.get_json(silent=True) or {}
    texto = (dados.get("texto") or "").strip()
    app_origem = (dados.get("app") or "").strip()

    if not texto:
        return jsonify({"erro": "Texto vazio"}), 400

    texto = texto[:4000]

    try:
        r = analisar_texto(app_origem, texto)
    except NotImplementedError:
        return jsonify({"erro": "Análise ainda não implementada"}), 501

    return jsonify({
        "suspeito": bool(r.get("suspeito", False)),
        "confianca": float(r.get("confianca", 0.0)),
        "motivos": list(r.get("motivos", [])),
    }), 200