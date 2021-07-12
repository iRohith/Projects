using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Mirror;

public class LocalController : NetworkBehaviour
{

    public float power = 10f, angularPower = 100f;
    private Camera _camera;
    [SerializeField]
    private Rigidbody2D rigidBody;
    [SerializeField]
    private float smoothSpeed = 0.025f;

    [SyncVar()]
    private Vector2 inputs = new Vector2();
    [SyncVar()]
    internal int score = 0;
    [SyncVar()]
    internal int chainNodes = 0;

    [SerializeField]
    private Chain chain = null;

    public InputManager inputManager;

    [Command]
    private void CmdSendInputs(Vector2 input){
        inputs = input;
    }
    
    // Start is called before the first frame update
    void Start()
    {
        _camera = FindObjectOfType<Camera>();
        if (rigidBody == null) rigidBody = GetComponent<Rigidbody2D>();
        chainNodes = chain.nodeCount;
        if (inputManager == null) inputManager = GameObject.Find("EventSystem").GetComponent<InputManager>();
    }

    void FixedUpdate()
    {
        if (isLocalPlayer) {
            inputManager.SetScore(score);
            var linputs = new Vector2(-inputManager.AxisX, inputManager.AxisY);
            if (linputs.y < 0) linputs.y = 0;
            if (isServer) inputs = linputs; else CmdSendInputs(linputs);
        }
        if (chainNodes != chain.nodeCount) { chain.nodeCount = chainNodes; }
        rigidBody.velocity = rigidBody.GetRelativeVector(Vector2.up) * power * inputs.y;
        rigidBody.angularVelocity = angularPower*inputs.x;

        if (isLocalPlayer && _camera != null){
            var pos = rigidBody.position;
            var destPos = new Vector2(pos.x, pos.y);
            var camPos = _camera.transform.position;
            var smoothPos = Vector2.Lerp(new Vector2(camPos.x, camPos.y), destPos, smoothSpeed);
            _camera.transform.position = new Vector3(smoothPos.x, smoothPos.y, -10);
            _camera.transform.rotation = Quaternion.Euler(0,0,Mathf.LerpAngle(_camera.transform.rotation.eulerAngles.z, rigidBody.rotation, smoothSpeed));
        }
    }

    
}
