package lukas.kreutzer.cityjsonviewer;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.GeometryUtils.*;
import com.badlogic.gdx.utils.ShortArray;

import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.feature.AbstractCityObjectType;
import org.citygml4j.cityjson.geometry.MultiSurfaceType;
import org.citygml4j.cityjson.geometry.SemanticsType;
import org.citygml4j.cityjson.geometry.SurfaceCollectionSemanticsObject;
import org.citygml4j.cityjson.geometry.TransformType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangulator {
    EarClippingTriangulator triangulator = new EarClippingTriangulator();
    CityJSON cityJson;

    public Triangulator(CityJSON cityjson) {
        this.cityJson = cityjson;
    }

    public List<List<Vector3>> triangulate(String surfacetype) {
        List <Vector3> vector3List = new ArrayList<Vector3>();
        List<List<Vector3>> vector3ListAll = new ArrayList<List<Vector3>>();
        //AbstractCityObjectType obj1 = cityJson.getCityObject("{10A31B7E-8FC2-45EE-836A-1DB9FEF06E04}");
        MultiSurfaceType mst;
        List<List<List<Integer>>> boundaries;
        List<List<List<Integer>>> boundariesFiltered;
        List<Integer> boundariesCurrent;
        List<List<Double>> VerticesList = cityJson.getVertices();
        int a;
        int b;
        List<List<Double>> VerticesListCurrent;

        float[] verticesFloat;
        ShortArray TriangleIndices;
        int xIndex;
        int yIndex;
        int zIndex;
        float x;
        float y;
        float z;
        Vector3 stuetzpunkt;
        int i;
        int j;
        double xmin;
        double xmax;
        double ymin;
        double ymax;
        double zmin;
        double zmax;
        double xDiff;
        double yDiff;
        double zDiff;
        String Case;
        int surfacetypeValue = -1;

        //Berechnung der BoundingBox und ihres Zentrums
        //Bei der BBox Berechnung werden die Transformationsparameter schon angebracht
        List<Double> BBox = cityJson.calcBoundingBox();
        List<Double> Center = new ArrayList<Double>();
        Center.add((BBox.get(3)+BBox.get(0))/2);
        Center.add((BBox.get(4)+BBox.get(1))/2);
        Center.add((BBox.get(5)+BBox.get(2))/2);

        //Auslesen der Transformationsparameter
        List<Double> scale = Arrays.asList(0.0, 0.0, 0.0);
        List<Double> translate = Arrays.asList(0.0, 0.0, 0.0);
        if (cityJson.isSetTransform()) {
            TransformType transform = cityJson.getTransform();
            scale = transform.getScale();
            translate = transform.getTranslate();
        }

        for (AbstractCityObjectType object : cityJson.getCityObjects()) {
            //System.out.println(object.getGeometry());

            //kann nur Objekte des MultisurfaceType triangulieren
            if (object.getGeometry().get(0) instanceof MultiSurfaceType) {
                mst = (MultiSurfaceType) object.getGeometry().get(0);

                //Überprüfung, ob uebergebener SurfaceType im Objekt vorhanden ist
                SurfaceCollectionSemanticsObject semantics = mst.getSemantics();
                List<SemanticsType> semanticstype = semantics.getSurfaces();
                List<Integer> semanticsvalues= semantics.getValues();
                for (i=0; i<semanticstype.size(); i++) {
                    if (semanticstype.get(i).getType().contentEquals(surfacetype) ) {
                        surfacetypeValue = i;
                    }
                }

                boundaries = mst.getSurfaces();
                boundariesFiltered = new ArrayList<List<List<Integer>>>();

                for (i=0; i<semanticsvalues.size(); i++) {
                    if (semanticsvalues.get(i)==surfacetypeValue) {
                        boundariesFiltered.add(boundaries.get(i));
                    }
                }

                for (a = 0; a < boundariesFiltered.size(); a++) {
                    for (b = 0; b < boundariesFiltered.get(a).size(); b++) {

                        VerticesListCurrent = new ArrayList<List<Double>>();
                        boundariesCurrent = boundariesFiltered.get(a).get(b);


                        for (Integer Int : boundariesCurrent) {
                            //System.out.println(Int);
                            //List<Double> VerticesTransformed = VerticesList.get(Int);
                            List<Double> VerticesTransformed = new ArrayList<>();

                            //Anwenden der Transformationsparameter und Anbringen der Reduktion um das Zentrum der Boundingbox
                            VerticesTransformed.add((VerticesList.get(Int).get(0) * scale.get(0) + translate.get(0)) - Center.get(0));
                            VerticesTransformed.add((VerticesList.get(Int).get(1) * scale.get(1) + translate.get(1)) - Center.get(1));
                            VerticesTransformed.add((VerticesList.get(Int).get(2) * scale.get(2) + translate.get(2)) - Center.get(2));

                            //aus irgendeinem Grund funktioniert dies nicht zuverlässig für alle Stuetzpunkte
                            //VerticesTransformed.set(0, (VerticesTransformed.get(0) * scale.get(0) + translate.get(0))-Center.get(0));
                            //VerticesTransformed.set(1, (VerticesTransformed.get(1) * scale.get(1) + translate.get(1))-Center.get(1));
                            //VerticesTransformed.set(2, (VerticesTransformed.get(2) * scale.get(2) + translate.get(2))-Center.get(2));


                            VerticesListCurrent.add(VerticesTransformed);
                        }

                        xmin = VerticesListCurrent.get(0).get(0);
                        xmax = VerticesListCurrent.get(0).get(0);
                        ymin = VerticesListCurrent.get(0).get(1);
                        ymax = VerticesListCurrent.get(0).get(1);
                        zmin = VerticesListCurrent.get(0).get(2);
                        zmax = VerticesListCurrent.get(0).get(2);

                        for (i = 1; i < VerticesListCurrent.size(); i++) {
                            if (VerticesListCurrent.get(i).get(0) < xmin) {
                                xmin = VerticesListCurrent.get(i).get(0);
                            }
                            if (VerticesListCurrent.get(i).get(0) > xmax) {
                                xmax = VerticesListCurrent.get(i).get(0);
                            }
                            if (VerticesListCurrent.get(i).get(1) < ymin) {
                                ymin = VerticesListCurrent.get(i).get(1);
                            }
                            if (VerticesListCurrent.get(i).get(1) > ymax) {
                                ymax = VerticesListCurrent.get(i).get(1);
                            }
                            if (VerticesListCurrent.get(i).get(2) < zmin) {
                                zmin = VerticesListCurrent.get(i).get(2);
                            }
                            if (VerticesListCurrent.get(i).get(2) > zmax) {
                                zmax = VerticesListCurrent.get(i).get(2);
                            }
                        }
                        xDiff = Math.abs(xmax - xmin);
                        yDiff = Math.abs(ymax - ymin);
                        zDiff = Math.abs(zmax - zmin);
                        Case = "0";

                        if (xDiff >= zDiff && yDiff >= zDiff) {
                            Case = "xy";
                        }

                        if (xDiff >= yDiff && zDiff >= yDiff) {
                            Case = "xz";
                        }

                        if (yDiff >= xDiff && zDiff >= xDiff) {
                            Case = "yz";
                        }

                        verticesFloat = new float[2 * VerticesListCurrent.size()];

                        switch (Case) {
                            case "xy":
                                for (i = 0; i < VerticesListCurrent.size(); i++) {
                                    verticesFloat[2 * i] = VerticesListCurrent.get(i).get(0).floatValue();
                                    verticesFloat[2 * i + 1] = VerticesListCurrent.get(i).get(1).floatValue();
                                }
                                GeometryUtils.ensureCCW(verticesFloat);
                                TriangleIndices = triangulator.computeTriangles(verticesFloat); //triangulator liefert Vertices in Reihenfolge der Dreiecksstuetzpunkte
                                for (i = 0; i < TriangleIndices.size; i++) {
                                    xIndex = TriangleIndices.get(i) * 2; // *2, da jeder Vertex 2 Werte (x,y) enthält
                                    yIndex = TriangleIndices.get(i) * 2 + 1; // *2, da jeder Vertex 2 Werte (x,y) enthält
                                    x = verticesFloat[xIndex];
                                    y = verticesFloat[yIndex];
                                    z = 123456789.123f; //default Wert zur Initialisierung

                                    for (j = 0; j < VerticesListCurrent.size(); j++) {
                                        if (VerticesListCurrent.get(j).get(0).floatValue() == x) {
                                            if (VerticesListCurrent.get(j).get(1).floatValue() == y) {
                                                z = VerticesListCurrent.get(j).get(2).floatValue();
                                                break; //Abbruch, sobald die zugehörige z Koordinate gefunden wurde
                                            }
                                        }
                                    }

                                    if (z == 123456789.123f) {
                                        System.out.println("Dreiecksstuetzpunkt nicht verwendbar");
                                        //Fehlermeldung falls z nicht auffindbar ist
                                    } else {
                                        stuetzpunkt = new Vector3(x, y, z);
                                        vector3List.add(stuetzpunkt);
                                    }
                                }
                                break;

                            case "xz":
                                for (i = 0; i < VerticesListCurrent.size(); i++) {
                                    verticesFloat[2 * i] = VerticesListCurrent.get(i).get(0).floatValue();
                                    verticesFloat[2 * i + 1] = VerticesListCurrent.get(i).get(2).floatValue();
                                }
                                GeometryUtils.ensureCCW(verticesFloat);
                                TriangleIndices = triangulator.computeTriangles(verticesFloat);
                                for (i = 0; i < TriangleIndices.size; i++) {
                                    xIndex = TriangleIndices.get(i) * 2; // *2, da jeder Vertex 2 Werte (x,z) enthält
                                    zIndex = TriangleIndices.get(i) * 2 + 1; // *2, da jeder Vertex 2 Werte (x,z) enthält
                                    x = verticesFloat[xIndex];
                                    y = 123456789.123f; //default Wert zur Initialisierung
                                    z = verticesFloat[zIndex];

                                    for (j = 0; j < VerticesListCurrent.size(); j++) {
                                        if (VerticesListCurrent.get(j).get(0).floatValue() == x) {
                                            if (VerticesListCurrent.get(j).get(2).floatValue() == z) {
                                                y = VerticesListCurrent.get(j).get(1).floatValue();
                                                break; //Abbruch, sobald die zugehörige y Koordinate gefunden wurde
                                            }
                                        }
                                    }

                                    if (y == 123456789.123f) {
                                        System.out.println("Dreiecksstuetzpunkt nicht verwendbar");
                                        //Fehlermeldung falls y nicht auffindbar ist
                                    } else {
                                        stuetzpunkt = new Vector3(x, y, z);
                                        vector3List.add(stuetzpunkt);
                                    }
                                }
                                break;

                            case "yz":
                                for (i = 0; i < VerticesListCurrent.size(); i++) {
                                    verticesFloat[2 * i] = VerticesListCurrent.get(i).get(1).floatValue();
                                    verticesFloat[2 * i + 1] = VerticesListCurrent.get(i).get(2).floatValue();
                                }
                                GeometryUtils.ensureCCW(verticesFloat);
                                TriangleIndices = triangulator.computeTriangles(verticesFloat);
                                for (i = 0; i < TriangleIndices.size; i++) {
                                    yIndex = TriangleIndices.get(i) * 2; // *2, da jeder Vertex 2 Werte (y,z) enthält
                                    zIndex = TriangleIndices.get(i) * 2 + 1; // *2, da jeder Vertex 2 Werte (y,z) enthält
                                    x = 123456789.123f; //default Wert zur Initialisierung
                                    y = verticesFloat[yIndex];
                                    z = verticesFloat[zIndex];

                                    for (j = 0; j < VerticesListCurrent.size(); j++) {
                                        if (VerticesListCurrent.get(j).get(1).floatValue() == y) {
                                            if (VerticesListCurrent.get(j).get(2).floatValue() == z) {
                                                x = VerticesListCurrent.get(j).get(0).floatValue();
                                                break; //Abbruch, sobald die zugehörige x Koordinate gefunden wurde
                                            }
                                        }
                                    }

                                    if (x == 123456789.123f) {
                                        System.out.println("Dreiecksstuetzpunkt nicht verwendbar");
                                        //Fehlermeldung falls x nicht auffindbar ist
                                    } else {
                                        stuetzpunkt = new Vector3(x, y, z);
                                        vector3List.add(stuetzpunkt);
                                    }
                                }
                                break;
                        }
                    }
                }
                vector3ListAll.add(vector3List);
                vector3List = new ArrayList<>(); //clear() reicht hier nicht, da sonst auch die Referenz in vector3ListAll gelöscht wird
            }
            else {
                System.out.println("Es handelt sich nicht um ein Multisurface");
            }
        }
        return vector3ListAll;
    }
}
