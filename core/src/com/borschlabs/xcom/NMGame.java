package com.borschlabs.xcom;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.borschlabs.xcom.gen.DungeonChamber;
import com.borschlabs.xcom.geometry.GeomUtils;
import com.borschlabs.xcom.geometry.Poly;
import com.borschlabs.xcom.geometry.VisibleMapBuilder;

import java.util.ArrayList;
import java.util.List;

public class NMGame extends ApplicationAdapter {
	private static final float PLAYER_SPEED = 15f;
	private static final float ROTATION_SPEED = 100f;
	private static final float CELL_SIZE = 2.f;
	private static final float VP_SIZE = 80f;
	private SpriteBatch mainBatch;
	private OrthographicCamera cam;
	private ShapeRenderer debugShapeRenderer;

	private Mesh floorMesh;

	private Sprite floorSprite;
	private Texture floorTexture;

	private ShaderProgram mainShaderProgram;

	private FrameBuffer visMapFB;
	private ShaderProgram visMapShaderProgram;
	private Mesh visMapMesh;

	private Poly bounds;
	private final List<Poly> geometry = new ArrayList<Poly>();
	private final Vector2 player = new Vector2();

	private Mesh debugMesh;

	private VisibleMapBuilder visibleMapBuilder;

	private BitmapFont font;
	private Texture fontTexture;
	private ShaderProgram fontShader;
	private SpriteBatch uiBatch;
	private float camRotation;

	private DungeonChamber chamber;
	private Vector3 bl;
	private Vector3 tl;
	private Vector3 tr;
	private Vector3 br;
	private ArrayList<Poly.Wall> walls;
	private float[] visMapVertices;
	private ArrayList<Poly.Wall> visibleWalls;

	private ShaderProgram wallShader;

	private Mesh wallsMesh;
	private float[] wallsVertices;
	private WallsStyle wallsStyle = WallsStyle.NORMAL;
	private Texture wallTexture;

	public NMGame() {
	}

	@Override
	public void create() {
		fontTexture = new Texture(Gdx.files.internal("arial_black.png"));
		fontTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		font = new BitmapFont(Gdx.files.internal("arial_black.fnt"), new TextureRegion(fontTexture), false);
		fontShader = new ShaderProgram(Gdx.files.internal("shaders/font.vsh"), Gdx.files.internal("shaders/font.fsh"));
		System.out.println(fontShader.isCompiled() ? "Font shader compiled!" : fontShader.getLog());

		uiBatch = new SpriteBatch();
		uiBatch.setShader(fontShader);

		NMInput inputProcessor = new NMInput();
		Gdx.input.setInputProcessor(inputProcessor);
		floorTexture = new Texture(Gdx.files.internal("textures/mosaic1.jpg"), true);
		floorTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		floorTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);
		floorSprite = new Sprite(floorTexture);

		floorMesh = new Mesh(false, 4, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"), new VertexAttribute(
			VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"));

		ShaderProgram.pedantic = false;

		mainShaderProgram = new ShaderProgram(Gdx.files.internal("shaders/main.vsh"), Gdx.files.internal("shaders/main.fsh"));
		System.out.println(mainShaderProgram.isCompiled() ? "Main shader compiled!" : mainShaderProgram.getLog());

		visMapShaderProgram = new ShaderProgram(Gdx.files.internal("shaders/vis_field.vsh"), Gdx.files.internal("shaders/vis_field.fsh"));
		System.out.println(visMapShaderProgram.isCompiled() ? "Lightmap shader compiled!" : visMapShaderProgram.getLog());

		visMapMesh = new Mesh(false, 4096, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"));

		debugMesh = new Mesh(true, 3, 0, new VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"));
		debugMesh.setVertices(new float[]{0f, 0f,
			100f, 100f,
			100f, 0f
		});

		wallShader = new ShaderProgram(Gdx.files.internal("shaders/wall.vsh"), Gdx.files.internal("shaders/wall.fsh"));
		System.out.println(wallShader.isCompiled() ? "Wall shader compiled!" : wallShader.getLog());

		wallsMesh = new Mesh(false, 4096, 0,
			new VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		wallTexture = new Texture(Gdx.files.internal("textures/brick_wall01.jpg"), true);
		wallTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
		wallTexture.setFilter(Texture.TextureFilter.MipMapLinearLinear, Texture.TextureFilter.MipMapLinearLinear);

		mainBatch = new SpriteBatch();
		mainBatch.setShader(mainShaderProgram);

		uiBatch = new SpriteBatch();
		uiBatch.setShader(fontShader);

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		cam = new OrthographicCamera();
		cam.update();
		debugShapeRenderer = new ShapeRenderer();

		player.set(20f, 20f);

		visibleMapBuilder = new VisibleMapBuilder(debugShapeRenderer);

		// build chamber
		chamber = new DungeonChamber();
		chamber.generate();
	}

	@Override
	public void resize(int width, int height) {
		float ar = width / (float)height;
		cam.setToOrtho(false, VP_SIZE * ar, VP_SIZE);

		if (visMapFB != null) {
			visMapFB.dispose();
		}

		visMapFB = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
		visMapFB.getColorBufferTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
		Matrix4 m = new Matrix4();
		m.setToOrtho2D(0, 0, width, height);
		uiBatch.setProjectionMatrix(m);
	}

	private void updateBounds() {
		float width = Gdx.graphics.getWidth();
		float height = Gdx.graphics.getHeight();

		bl = cam.unproject(new Vector3(0, height, 0));
		tl = cam.unproject(new Vector3(0, 0, 0));
		tr = cam.unproject(new Vector3(width, 0, 0));
		br = cam.unproject(new Vector3(width, height, 0));

		bounds = new Poly(new Vector2(bl.x, bl.y), new Vector2(tl.x, tl.y),
			new Vector2(tr.x, tr.y), new Vector2(br.x, br.y));

		floorMesh.setVertices(new float[]{
			-1000, -1000, 0, 0,
			-1000, 1000, 0, 100,
			1000, 1000, 100, 100,
			1000, -1000, 100, 0
		});
	}

	@Override
	public void render() {
		processInput();

		cam.position.set(player.x, player.y, 0);
		cam.update();

		updateBounds();

		renderVisMap();

		debugShapeRenderer.setProjectionMatrix(cam.combined);

		//Gdx.gl.glClearColor(0.45f, 0.52f, 0f, 1f);
		Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		drawWalls();

		drawSprites();

		// player
		debugShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		debugShapeRenderer.setColor(Color.RED);
		debugShapeRenderer.circle(player.x, player.y, 0.2f);
		debugShapeRenderer.end();

		uiBatch.begin();
		font.setColor(Color.WHITE);
		font.draw(uiBatch, "Hello epta!!! " + Gdx.graphics.getFramesPerSecond() + " fps", 10, Gdx.graphics.getHeight() - 10);

		font.draw(uiBatch, "wasd - movement; q,e - rotation; scroll - zooming", 10, Gdx.graphics.getHeight() - 50);
		uiBatch.end();

		drawDebugGeometry();
	}

	private void drawSprites() {

		mainBatch.setProjectionMatrix(cam.combined);

		mainBatch.begin();

		// set vis map
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE1);
		visMapFB.getColorBufferTexture().bind();
		mainShaderProgram.setUniformi("u_vismap", 1);

		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);

		drawFloor();
		//floorSprite.draw(mainBatch);
		//mainBatch.draw(visMapFB.getColorBufferTexture(), 0, 0);
		mainBatch.end();
	}

	private void drawFloor() {
		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		floorTexture.bind();
		//mainShaderProgram.setUniformi("u_sampled2D", 0);
		floorMesh.render(mainShaderProgram, GL20.GL_TRIANGLE_FAN, 0, 4);
	}

	private void drawWalls() {
		int verticesCount = visibleWalls.size() * 6;

		int bufSize = wallsMesh.getVertexSize() * verticesCount;
		if (wallsVertices == null || wallsVertices.length < bufSize) {
			wallsVertices = new float[bufSize * 2];
		}

		int idx = 0;
		for (Poly.Wall w : visibleWalls) {
			/*Vector2 bl = w.corners[0];
			Vector2 br = w.corners[1];
			Vector2 to0Dir = bl.cpy().sub(player);
			Vector2 to1Dir = br.cpy().sub(player);

			float dist0 = (float) Math.sqrt(to0Dir.len() / 4f);
			float dist1 = (float) Math.sqrt(to1Dir.len() / 4f);
			to0Dir.nor();
			to1Dir.nor();
			to0Dir.scl(dist0 + 0.3f);
			to1Dir.scl(dist1 + 0.3f);

			Vector2 tl = w.corners[0].cpy().add(to0Dir);
			Vector2 tr = w.corners[1].cpy().add(to1Dir);*/

			/*Vector2 to0Dir = w.corners[0].cpy().sub(player).scl(0.1f);
			Vector2 to1Dir = w.corners[1].cpy().sub(player).scl(0.1f);
			debugShapeRenderer.line(w.corners[0], w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[0]);
			debugShapeRenderer.line(w.corners[1].cpy().add(to1Dir), w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[1].cpy().add(to1Dir));*/

			Vector2 bl = w.corners[0];
			Vector2 br = w.corners[1];
			Vector2 to0Dir = bl.cpy().sub(player);
			Vector2 to1Dir = br.cpy().sub(player);

			float dist0 = (float) Math.sqrt(to0Dir.len() / 5f);
			float dist1 = (float) Math.sqrt(to1Dir.len() / 5f);
					to0Dir.nor();
					to1Dir.nor();
			to0Dir.scl(dist0 + 0.5f);
			to1Dir.scl(dist1 + 0.5f);

			Vector2 tl = w.corners[0].cpy().add(to0Dir);
			Vector2 tr = w.corners[1].cpy().add(to1Dir);

			float color = Color.GRAY.toFloatBits();
			Color color2 = new Color(Color.GRAY);
			color2.a = 0.0f;
			float transparent = color;////color2.toFloatBits();

			wallsVertices[idx++] = bl.x;
			wallsVertices[idx++] = bl.y;
			wallsVertices[idx++] = color;
			wallsVertices[idx++] = 0f;
			wallsVertices[idx++] = 0f;

			wallsVertices[idx++] = tl.x;
			wallsVertices[idx++] = tl.y;
			wallsVertices[idx++] = transparent;
			wallsVertices[idx++] = 0f;
			wallsVertices[idx++] = 1f;

			wallsVertices[idx++] = tr.x;
			wallsVertices[idx++] = tr.y;
			wallsVertices[idx++] = transparent;
			wallsVertices[idx++] = 1f;
			wallsVertices[idx++] = 1f;

			wallsVertices[idx++] = tr.x;
			wallsVertices[idx++] = tr.y;
			wallsVertices[idx++] = transparent;
			wallsVertices[idx++] = 1f;
			wallsVertices[idx++] = 1f;

			wallsVertices[idx++] = br.x;
			wallsVertices[idx++] = br.y;
			wallsVertices[idx++] = color;
			wallsVertices[idx++] = 1f;
			wallsVertices[idx++] = 0f;

			wallsVertices[idx++] = bl.x;
			wallsVertices[idx++] = bl.y;
			wallsVertices[idx++] = color;
			wallsVertices[idx++] = 0f;
			wallsVertices[idx++] = 0f;
		}

		wallShader.begin();

		Gdx.graphics.getGL20().glActiveTexture(GL20.GL_TEXTURE0);
		wallTexture.bind(0);

		wallShader.setUniformf("u_centerPoint", player);
		wallShader.setUniformi("u_sampled2D", 0);
		wallShader.setUniformi("u_vismap", 1);

		wallShader.setUniformMatrix("u_projTrans", cam.combined);
		wallsMesh.setVertices(wallsVertices, 0, idx);
		wallsMesh.render(wallShader, GL20.GL_TRIANGLES, 0, verticesCount);

		wallShader.end();

		/*debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		debugShapeRenderer.setColor(Color.BLUE);

		for (Poly.Wall w : visibleWalls) {
			Vector2 to0Dir = w.corners[0].cpy().sub(player).scl(0.1f);
			Vector2 to1Dir = w.corners[1].cpy().sub(player).scl(0.1f);
			debugShapeRenderer.line(w.corners[0], w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[0]);
			debugShapeRenderer.line(w.corners[1].cpy().add(to1Dir), w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[1].cpy().add(to1Dir));
		}

		debugShapeRenderer.end();*/
	}

	private void renderVisMap() {
		geometry.clear();
		walls = new ArrayList<Poly.Wall>();
		DungeonChamber.Field f = chamber.field;
		for (DungeonChamber.Cell c : f) {
			if (c.wall && cellInViewPort(c)) {
				/*Vector2 lb = new Vector2(c.x * CELL_SIZE, c.y * CELL_SIZE);
				Vector2 rb = new Vector2((c.x + 1f) * CELL_SIZE, c.y * CELL_SIZE);
				Vector2 rt = new Vector2((c.x + 1f) * CELL_SIZE, (c.y + 1f) * CELL_SIZE);
				Vector2 lt = new Vector2(c.x * CELL_SIZE, (c.y + 1f) * CELL_SIZE);

				walls.add(new Poly.Wall(lb, rb));
				walls.add(new Poly.Wall(rb, rt));
				walls.add(new Poly.Wall(rt, lt));
				walls.add(new Poly.Wall(lt, lb));*/
				List<Poly.Wall> cellGeometry = f.getCellGeometry(c, CELL_SIZE);
				for (Poly.Wall w : cellGeometry) {
					if (GeomUtils.wallFrontFacing(player, w)) {
						walls.add(w);
					}
				}
				//walls.addAll(cellGeometry);
			}
		}

		//walls.clear();

		walls.add(new Poly.Wall(new Vector2(bl.x, bl.y), new Vector2(tl.x, tl.y)));
		walls.add(new Poly.Wall(new Vector2(tl.x, tl.y), new Vector2(tr.x, tr.y)));
		walls.add(new Poly.Wall(new Vector2(tr.x, tr.y), new Vector2(br.x, br.y)));
		walls.add(new Poly.Wall(new Vector2(br.x, br.y), new Vector2(bl.x, bl.y)));

		List<Vector2> outputPoints = new ArrayList<Vector2>();
		visibleMapBuilder.build(player, walls, bounds, outputPoints);
		outputPoints.add(0, player);

		int verticesCount = outputPoints.size();
		if (visMapVertices == null || visMapVertices.length / 2 < verticesCount) {
			visMapVertices = new float[verticesCount * 2];
		}

		visibleWalls = new ArrayList<Poly.Wall>();

		for (int i = 0; i < outputPoints.size(); ++i) {
			Vector2 prev = i > 0 ? outputPoints.get(i - 1) : null;
			Vector2 next = outputPoints.get(i);
			visMapVertices[i * 2] = next.x;
			visMapVertices[i * 2 + 1] = next.y;

			if (prev != null) {
				Vector2 toWallDir = player.cpy().sub(prev);
				Vector2 wallDir = next.cpy().sub(prev);
				if (!GeomUtils.isOnLine(toWallDir, wallDir)) {
					visibleWalls.add(new Poly.Wall(prev, next));
				}
			}
		}
		visMapMesh.setVertices(visMapVertices);

		visMapFB.begin();
		Gdx.gl.glClearColor(0.0f, 0.0f, 0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		visMapShaderProgram.begin();
		visMapShaderProgram.setUniformf("u_centerPoint", player);
		visMapShaderProgram.setUniformMatrix("u_projTrans", cam.combined);
		visMapMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, verticesCount);

		//debugMesh.render(visMapShaderProgram, GL20.GL_TRIANGLE_FAN, 0, 3);

		visMapShaderProgram.end();
		visMapFB.end();
	}

	private boolean cellInViewPort(DungeonChamber.Cell c) {
		Rectangle bb = new Rectangle(bl.x, bl.y, tr.x - bl.x, tr.y - bl.y);
		Rectangle tmp = new Rectangle(c.x * CELL_SIZE, c.y * CELL_SIZE, CELL_SIZE, CELL_SIZE);
		return bb.overlaps(tmp);
	}

	private void processInput() {
		float dt = Gdx.graphics.getDeltaTime();

		Vector2 dir = new Vector2();
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			dir.x = -1f;
		} else if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			dir.x = 1f;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			dir.y = 1f;
		} else if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			dir.y = -1;
		}

		if (Gdx.input.isKeyPressed(Input.Keys.E)) {
			cam.rotate(Gdx.graphics.getDeltaTime() * ROTATION_SPEED);
		} else if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
			cam.rotate(-Gdx.graphics.getDeltaTime() * ROTATION_SPEED);
		}

		dir.nor();

		Vector3 toTop = new Vector3(Vector3.Y);
		toTop.mul(cam.combined);

		float a = new Vector2(toTop.x, toTop.y).angleRad(Vector2.Y);

		player.add(dir.scl(dt * PLAYER_SPEED));
	}

	private void drawDebugGeometry() {
		/*debugShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		debugShapeRenderer.setColor(1, 1, 1, 0.3f);
		for (Poly p : geometry) {
			p.draw(debugShapeRenderer);
		}

		for (Poly.Wall w : walls) {
			debugShapeRenderer.line(w.corners[0], w.corners[1]);
		}

		/*debugShapeRenderer.setColor(Color.RED);
		for (Poly.Wall w : visibleWalls) {
			Vector2 to0Dir = w.corners[0].cpy().sub(player).scl(0.1f);
			Vector2 to1Dir = w.corners[1].cpy().sub(player).scl(0.1f);
			debugShapeRenderer.line(w.corners[0], w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[0]);
			debugShapeRenderer.line(w.corners[1].cpy().add(to1Dir), w.corners[1]);
			debugShapeRenderer.line(w.corners[0].cpy().add(to0Dir), w.corners[1].cpy().add(to1Dir));
		}

		debugShapeRenderer.end();*/
	}

	@Override
	public void dispose() {
		debugShapeRenderer.dispose();
		mainBatch.dispose();
		debugMesh.dispose();
		floorTexture.dispose();
		visMapFB.dispose();
		visMapMesh.dispose();
		visMapShaderProgram.dispose();
	}

	private class NMInput extends InputAdapter {
		@Override
		public boolean scrolled(int amount) {
			cam.zoom += amount * Gdx.graphics.getDeltaTime() * 3f;
			return true;
		}
	}
}
