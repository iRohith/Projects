using System.Collections;
using System.Collections.Generic;
using UnityEngine;

//[ExecuteInEditMode]
public class Chain : MonoBehaviour
{
    [Range(2, 1000)]
    public int nodeCount = 3;
    [SerializeField]
    float spacing = 0.1f, anchorSpacing = 0.1f, endObjectSpacing = 0.1f;
    [SerializeField]
    Vector2 direction = Vector2.up;
    [SerializeField]
    GameObject rootNode;
    [SerializeField]
    GameObject endAttachObject = null;

    List<GameObject> nodes = new List<GameObject>();
    
    // Start is called before the first frame update
    void Start()
    {
        if (rootNode == null) rootNode = GameObject.Find("node0");
        rootNode.GetComponent<HingeJoint2D>().connectedBody = GameObject.Find("Ring").GetComponent<Rigidbody2D>();
        nodes.Add(rootNode);
        var node = Instantiate<GameObject>(rootNode, transform);
        node.name = "node1";
        var hinge = node.GetComponent<HingeJoint2D>();
        hinge.connectedBody = rootNode.GetComponent<Rigidbody2D>();
        direction.Normalize();
        hinge.anchor = -direction*anchorSpacing;
        direction = direction * spacing;
        node.transform.Translate(direction.x, direction.y, 0);

        if (endAttachObject != null){
            var eTransform = endAttachObject.transform;
            var lTransform = node.transform;
            var dir = lTransform.TransformDirection(direction);
            //eTransform.rotation = Quaternion.Euler(0, 0, Vector3.Angle(lTransform.rotation * Vector3.up, Vector3.up) + lTransform.parent.rotation.eulerAngles.z);
            eTransform.rotation = Quaternion.Euler(0, 0, Vector2.SignedAngle(Vector2.up, lTransform.TransformDirection(direction.normalized)));
            eTransform.position = lTransform.position;
            eTransform.Translate(dir * endObjectSpacing, Space.World);
            var joint = node.AddComponent<HingeJoint2D>();
            joint.enableCollision = true;
            joint.connectedBody = endAttachObject.GetComponent<Rigidbody2D>();
            joint.anchor = direction*anchorSpacing;
        }
        nodes.Add(node);
        if (nodeCount - 2 > 0) AddNodes(nodeCount - 2);
    }

    void Update(){
        if (nodeCount < 2) nodeCount = 2;
        if (nodeCount > 1 && nodes.Count != nodeCount){
            AddNodes(nodeCount - nodes.Count);
            RemoveNodes(nodes.Count - nodeCount);
        }
    }

    void AddNodes(int count){
        if (count <= 0) return;
        var lastNode = nodes[nodes.Count - 1];
        GameObject newNode = nodes[nodes.Count-2];
        Rigidbody2D rb = newNode.GetComponent<Rigidbody2D>();
        for (int i=0; i<count; i++){
            newNode = Instantiate<GameObject>(newNode, transform);
            var hinge = newNode.GetComponent<HingeJoint2D>();
            if (i==0) {
                hinge.anchor = -direction*anchorSpacing;
                newNode.transform.position = lastNode.transform.position;
                newNode.transform.rotation = lastNode.transform.rotation;
            } else newNode.transform.Translate(direction.x, direction.y, 0);
            hinge.connectedBody = rb;
            newNode.name = "node" + (nodes.Count - 1);
            nodes.Insert(nodes.Count-1, newNode);
            rb = newNode.GetComponent<Rigidbody2D>();
        }
        lastNode.transform.Translate(count*new Vector3(direction.x, direction.y));
        lastNode.GetComponent<HingeJoint2D>().connectedBody = rb;
        lastNode.name = "node" + (nodes.Count - 1);
        
        if (endAttachObject != null)
            endAttachObject.transform.Translate(lastNode.transform.TransformDirection(direction.normalized) * spacing * count, Space.World);
    }

    void RemoveNodes(int count){
        if (count <= 0 || nodes.Count - count < 2) return;
        var lastNode = nodes[nodes.Count - 1];
        int index = nodes.Count - 1 - count;
        var lastNode1 = nodes[index];
        lastNode.transform.position = lastNode1.transform.position;
        lastNode.transform.rotation = lastNode1.transform.rotation;
        lastNode.GetComponent<HingeJoint2D>().connectedBody = nodes[index-1].GetComponent<Rigidbody2D>();
        for (int i=0; i<count; i++) Destroy(nodes[index+i]);
        nodes.RemoveRange(index, count);

        if (endAttachObject != null){
            var eTransform = endAttachObject.transform;
            var lTransform = lastNode.transform;
            var dir = lTransform.TransformDirection(direction);
            eTransform.rotation = Quaternion.Euler(0, 0, Vector2.SignedAngle(Vector2.up, lTransform.TransformDirection(direction.normalized)));
            eTransform.position = lTransform.position;
            eTransform.Translate(dir * endObjectSpacing, Space.World);
        }
    }

}
