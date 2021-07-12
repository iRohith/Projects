using System.Collections;
using System.Collections.Generic;
using UnityEngine;

[ExecuteInEditMode]
[RequireComponent (typeof(EdgeCollider2D), typeof(LineRenderer))]
public class OuterZone : MonoBehaviour
{

    public float radius = 10f;
    public int NumPoints = 32;
    private LineRenderer lineRenderer;

    EdgeCollider2D EdgeCollider;
    float currentRadius;

    // Start is called before the first frame update
    void Start()
    {
        DrawCircle(transform.position);
        CreateColliderCircle();
    }

    void Update(){
        if (currentRadius != radius)
            DrawCircle(transform.position);
        if (currentRadius != radius || NumPoints != EdgeCollider.pointCount)
            CreateColliderCircle();
    }

    public void DrawCircle(Vector3 center)
    {
        lineRenderer = GetComponent<LineRenderer>();
        var segments = 360;
        lineRenderer.positionCount = segments + 1;

        var pointCount = segments + 1; // add extra point to make startpoint and endpoint the same to close the circle
        var points = new Vector3[pointCount];
        
        for (int i = 0; i < pointCount; i++)
        {
            var rad = Mathf.Deg2Rad * (i * 360f / segments);
            points[i] = new Vector3(Mathf.Sin(rad) * radius, Mathf.Cos(rad) * radius) - center;
        }

        lineRenderer.SetPositions(points);
        currentRadius = radius;
    }

    void CreateColliderCircle()
    {
        Vector2[] edgePoints = new Vector2[NumPoints + 1];
        EdgeCollider = GetComponent<EdgeCollider2D>();
        float radiusDiff = lineRenderer.startWidth / 2;
       
        for(int loop = 0; loop <= NumPoints; loop++)
        {
            float angle = (Mathf.PI * 2.0f / NumPoints) * loop;
            edgePoints[loop] = new Vector2(Mathf.Sin(angle), Mathf.Cos(angle)) * (radius - radiusDiff);
        }
       
        EdgeCollider.points = edgePoints;
        currentRadius = radius;
    }
}
