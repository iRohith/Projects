using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class InputManager : MonoBehaviour
{
    public FixedJoystick fixedJoystick;
    public Text scoreBox;
    
    public void SetScore(int score){
        if (scoreBox != null) scoreBox.text = "Score : " + score;
    }

    public bool fromJoystick {
        get {
            return fixedJoystick != null && fixedJoystick.enabled;
        }
    }

    public float AxisX {
        get {
            if (!fromJoystick) return Input.GetAxis("Horizontal");
            return fixedJoystick.Horizontal;
        }
    }
    public float AxisY {
        get {
            if (!fromJoystick) return Input.GetAxis("Vertical");
            return fixedJoystick.Vertical;
        }
    }
}
