package lukas.kreutzer.cityjsonviewer;


import android.content.Context;
import android.net.Uri;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.cityjson.geometry.MultiSurfaceType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Viewer extends ApplicationAdapter implements GestureDetector.GestureListener {
	public PerspectiveCamera cam;
	public ModelBatch modelBatch;
	public Model model;
	public ModelInstance instance;
	public Environment environment;
	public CameraInputController camController;
	public CityJSON cityJson;
	public Uri uri;
	public Context context;
	//public GestureDetector gestureDetector;

	public Viewer(Uri uri, Context context) {
		this.uri= uri;
		this.context = context;
	}


	@Override
	public void create () {

		CityJSONParser parser = new CityJSONParser(uri, context);
		try {
			cityJson = parser.parse();
		} catch (IOException e) {
			e.printStackTrace();
		}

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

		modelBatch = new ModelBatch();



		int attr = VertexAttributes.Usage.Position | Usage.ColorPacked;
		Triangulator triangulator = new Triangulator(cityJson);
		List<List<Vector3>> vector3ListRoof = triangulator.triangulate("RoofSurface");
		List<List<Vector3>> vector3ListGround = triangulator.triangulate("GroundSurface");
		List<List<Vector3>> vector3ListWall = triangulator.triangulate("WallSurface");

		ModelBuilder modelBuilder = new ModelBuilder();
		//model = modelBuilder.createBox(5f, 5f, 5f,
				//new Material(ColorAttribute.createDiffuse(Color.GREEN)),
				//Usage.Position | Usage.Normal);
		//instance = new ModelInstance(model,0f,0f,0f);

		modelBuilder.begin();

		List<Double> BBox = cityJson.calcBoundingBox();
		List<Double> Center = new ArrayList<Double>();
		Center.add((BBox.get(3)+BBox.get(0))/2);
		Center.add((BBox.get(4)+BBox.get(1))/2);
		Center.add((BBox.get(5)+BBox.get(2))/2);
		Vector3 CenterV3 = new Vector3(Center.get(0).floatValue(), Center.get(1).floatValue(), Center.get(2).floatValue());

		Array<Attribute> AttributeArrayRoof = new Array<>();
		AttributeArrayRoof.add(ColorAttribute.createDiffuse(Color.RED));
		AttributeArrayRoof.add(IntAttribute.createCullFace(GL20.GL_NONE));
		Material materialRoof = new Material(AttributeArrayRoof);

		Array<Attribute> AttributeArrayGround = new Array<>();
		AttributeArrayGround.add(ColorAttribute.createDiffuse(Color.DARK_GRAY));
		AttributeArrayGround.add(IntAttribute.createCullFace(GL20.GL_NONE));
		Material materialGround = new Material(AttributeArrayGround);

		Array<Attribute> AttributeArrayWall = new Array<>();
		AttributeArrayWall.add(ColorAttribute.createDiffuse(Color.LIGHT_GRAY));
		AttributeArrayWall.add(IntAttribute.createCullFace(GL20.GL_NONE));
		Material materialWall = new Material(AttributeArrayWall);

		int counter = 0;
		for (int i = 0; i<vector3ListRoof.size(); i++) {
			for (int v = 0; v < vector3ListRoof.get(i).size() / 3; v++) {
				Vector3 t1 = vector3ListRoof.get(i).get(v * 3);
				Vector3 t2 = vector3ListRoof.get(i).get(v * 3 + 1);
				Vector3 t3 = vector3ListRoof.get(i).get(v * 3 + 2);


				modelBuilder.part("Dreieck " + counter, GL20.GL_TRIANGLES, attr, materialRoof)
						.triangle(t1, t2, t3);
				counter ++;
			}
			for (int v = 0; v < vector3ListGround.get(i).size() / 3; v++) {
				Vector3 t1 = vector3ListGround.get(i).get(v * 3);
				Vector3 t2 = vector3ListGround.get(i).get(v * 3 + 1);
				Vector3 t3 = vector3ListGround.get(i).get(v * 3 + 2);

				modelBuilder.part("Dreieck " + counter, GL20.GL_TRIANGLES, attr, materialGround)
						.triangle(t1, t2, t3);
				counter ++;
			}
			for (int v = 0; v < vector3ListWall.get(i).size() / 3; v++) {
				Vector3 t1 = vector3ListWall.get(i).get(v * 3);
				Vector3 t2 = vector3ListWall.get(i).get(v * 3 + 1);
				Vector3 t3 = vector3ListWall.get(i).get(v * 3 + 2);

				modelBuilder.part("Dreieck " + counter, GL20.GL_TRIANGLES, attr, materialWall)
						.triangle(t1, t2, t3);
				counter ++;
			}
		}
		model = modelBuilder.end();
		instance = new ModelInstance(model);

		AbstractCityObjectType obj1 = cityJson.getCityObject("{10A31B7E-8FC2-45EE-836A-1DB9FEF06E04}");
		MultiSurfaceType mst1 = (MultiSurfaceType) obj1.getGeometry().get(0);
		List<List<List<Integer>>> boundaries1 = mst1.getSurfaces();
		System.out.println(boundaries1.get(0).get(0));
		List<List<Double>> VerticesList = cityJson.getVertices();
		System.out.println(VerticesList.get(0));





		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//cam.position.set(instance.model.meshParts.get(0).center.x+10f, instance.model.meshParts.get(0).center.y+10f, instance.model.meshParts.get(0).center.z+10f);
		//cam.lookAt(instance.model.meshParts.get(0).center);
		//cam.lookAt(Center.get(0).floatValue(),Center.get(1).floatValue(),Center.get(2).floatValue());
		cam.position.set(0f,0f,300f);
		cam.lookAt(0f,0f,0f);
		cam.near = 0.1f;
		cam.far = 10000f;
		cam.update();


		camController = new CameraInputController(cam);
		Gdx.input.setInputProcessor(camController);

		//gestureDetector = new GestureDetector(this);
		//Gdx.input.setInputProcessor(gestureDetector);
	}

	@Override
	public void render () {
		camController.update();

		Gdx.gl.glClearColor(255, 255, 240, 1);
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_TEXTURE_BINDING_2D);


		modelBatch.begin(cam);
		modelBatch.render(instance, environment);
		modelBatch.end();
	}

	@Override
	public void dispose () {
		modelBatch.dispose();
		model.dispose();
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}
}
