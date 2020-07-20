package com.mygdx.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class FlappyBird extends ApplicationAdapter{
	//classe SpriteBatch cria as animações
	private SpriteBatch batch;

	private Texture[] personagem;
	private Texture monster;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	private Random numRandom;

	private BitmapFont fonte;
	private BitmapFont mensagem;

	private Circle passaroCiculo;

	private Rectangle canoTopoR;
	private Rectangle canoBaixoR;

	private ShapeRenderer shape;

	//Atributos de config
	private float larguraDevice;
	private float alturaDevice;
	private int pontos = 0;

	//private boolean estadoJogo = false; // jogo ñ iniciado
	private int estadoJogo = 0;
	private boolean marcouPonto;

	private float posicaoMovimentoCanoVertical;
	private float deltaTime;
	private float alturaEntraCanosRandomica;
	private float variacao = 0;
	private float velocidadeQueda = 0;
	private float posicaoInicialVertical = 0;
	private float posicaoMovimentoCanoHorizontal;
	private float espacoEntreCanos;

	//camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float virtual_width = 768;
	private final float virtual_heigth = 1024;

	private Texture[] getTextureArray(String[] fileArray) {
		Texture[] a = new Texture[fileArray.length];
		for(int i=0; i < fileArray.length; i++) {
			a[i] = new Texture(fileArray[i]);
		}

		return a;
	}

	@Override
	public void create () {
		//inicializar jogo
		//Gdx.app.log("Create", "Jogo Inicializado");

		fonte = new BitmapFont();
		fonte.setColor(Color.WHITE);
		fonte.getData().setScale(6);

		mensagem = new BitmapFont();
		mensagem.setColor(Color.WHITE);
		mensagem.getData().setScale(3);

		batch = new SpriteBatch();
		passaroCiculo = new Circle();
//		canoBaixoR = new Rectangle();
//		canoTopoR = new Rectangle();
//		shape = new ShapeRenderer();
		personagem = getTextureArray(new String[] { "passaro1.png", "passaro2.png", "passaro3.png" });
//		personagem = getTextureArray(new String[] { "gmedeiros.PNG" });

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");

		//config camera
		camera = new OrthographicCamera();
		camera.position.set(virtual_width/2, virtual_heigth/2, 0);
		viewport = new StretchViewport(virtual_width, virtual_heigth, camera);

		larguraDevice = virtual_width;
		alturaDevice  = virtual_heigth;

		posicaoInicialVertical = alturaDevice / 2;
		posicaoMovimentoCanoHorizontal = larguraDevice - 150;
		espacoEntreCanos = 290;

	}

	@Override
	public void render () {
		//chamado de tempos em tempos para renderizar
		camera.update();

		//Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		numRandom = new Random();
		deltaTime = Gdx.graphics.getDeltaTime();
		variacao += deltaTime * 10;
		if (variacao >= personagem.length) variacao = 0;

		if (estadoJogo == 0) {
			if (Gdx.input.justTouched()) {
				estadoJogo = 1;
			}
		} else {

			velocidadeQueda++;
			if (posicaoInicialVertical > 0 || velocidadeQueda < 0)
				posicaoInicialVertical -= velocidadeQueda;

			if(estadoJogo == 1){
				posicaoMovimentoCanoHorizontal -= deltaTime * 200;

				if (Gdx.input.justTouched()) {
					velocidadeQueda = -20;
				}

				//verifica se o cano saiu da tela
				if (posicaoMovimentoCanoHorizontal < -canoTopo.getWidth()) {
					//cano "sai" da tela para aparecer outro
					posicaoMovimentoCanoHorizontal = larguraDevice;
					alturaEntraCanosRandomica = numRandom.nextInt(400) - 200;
					marcouPonto = false;
				}
				//verifica ponto
				//posição do passaro x = 120
				if (posicaoMovimentoCanoHorizontal < 120) {
					if (!marcouPonto) {
						pontos++;
						marcouPonto = true;
					}
				}
			}else {
				//Game Over

				if (Gdx.input.justTouched()){
					estadoJogo = 0;
					pontos = 0;
					velocidadeQueda = 0;
					posicaoInicialVertical = alturaDevice/2;
					posicaoMovimentoCanoHorizontal = larguraDevice;
				}
			}
		}

		//Config dados de projeção da camera
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		//render da imagem
		//ordem importa, draw funciona por camadas
		batch.draw(fundo,0,0, larguraDevice, alturaDevice);
		batch.draw(canoTopo, posicaoMovimentoCanoHorizontal, alturaDevice/2 + espacoEntreCanos / 2 + alturaEntraCanosRandomica);
		batch.draw(canoBaixo, posicaoMovimentoCanoHorizontal, alturaDevice/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + alturaEntraCanosRandomica);
		batch.draw(personagem[(int) variacao], 120, posicaoInicialVertical);
		fonte.draw(batch, String.valueOf(pontos), larguraDevice/2, alturaDevice - 60);
		if (estadoJogo == 2){
			batch.draw(gameOver, larguraDevice/2 - gameOver.getWidth()/2, alturaDevice/2 - gameOver.getHeight()/2);
			mensagem.draw(batch,"Você é muito Ruim\nToque para reiniciar", larguraDevice/2 - 200, alturaDevice/2 - gameOver.getHeight()/2);
		}

		batch.end();

		passaroCiculo.set(120 + personagem[(int) variacao].getWidth()/2, posicaoInicialVertical + personagem[(int) variacao].getHeight()/2, personagem[(int) variacao].getWidth()/2);
		canoBaixoR = new Rectangle(
			//x,y,width, height
		posicaoMovimentoCanoHorizontal, alturaDevice/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + alturaEntraCanosRandomica,	canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		canoTopoR = new Rectangle(
				//x,y,width, height
		posicaoMovimentoCanoHorizontal, alturaDevice/2 + espacoEntreCanos / 2 + alturaEntraCanosRandomica, canoTopo.getWidth(), canoTopo.getHeight()
		);

		//Desenhar formas, valida se a ertrutura para a colisão está certa
//		shape.begin(ShapeRenderer.ShapeType.Filled);//Forma preenchida
//		shape.circle(passaroCiculo.x, passaroCiculo.y, passaroCiculo.radius);
//		shape.rect(canoBaixoR.x, canoBaixoR.y, canoBaixoR.width, canoBaixoR.height);
//		shape.rect(canoTopoR.x,  canoTopoR.y,  canoTopoR.width,  canoTopoR.height);
//		shape.setColor(Color.WHITE);
//		shape.end();

		//colisão
		if(Intersector.overlaps(passaroCiculo, canoBaixoR) || Intersector.overlaps(passaroCiculo, canoTopoR) || posicaoInicialVertical <=0 || posicaoInicialVertical>=alturaDevice){
			//Gdx.app.log("gameOver", "bateu você é muito ruim");
			estadoJogo = 2;
		}

		}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}
}

