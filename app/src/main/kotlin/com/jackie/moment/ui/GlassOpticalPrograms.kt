package com.jackie.moment.ui

import android.graphics.RuntimeShader
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.toArgb

/** App-owned backdrop sampling only. Floating optics transmit with alpha;
 * Android's compositor, not this shader, supplies the other app's window. */
@RequiresApi(33)
internal class GlassShader(private val lens: Boolean) {
    private val shader = RuntimeShader(SceneProgram + if (lens) LensProgram else BackgroundProgram)
    val brush = ShaderBrush(shader)

    fun update(size: Size, origin: Offset, sceneSize: Size, dark: Float, accent: Color, tint: Color,
        radius: Float, press: Float, density: Float, floating: Boolean = false, diagnostic: Boolean = false) {
        shader.setFloatUniform("sceneSize", sceneSize.width.coerceAtLeast(1f), sceneSize.height.coerceAtLeast(1f))
        shader.setFloatUniform("dark", dark)
        shader.setFloatUniform("density", density)
        shader.setFloatUniform("diagnostic", if (diagnostic) 1f else 0f)
        shader.setColorUniform("accentColor", accent.toArgb())
        if (lens) {
            shader.setFloatUniform("panelSize", size.width, size.height)
            shader.setFloatUniform("origin", origin.x, origin.y)
            shader.setFloatUniform("radius", radius)
            shader.setFloatUniform("press", press)
            shader.setFloatUniform("floating", if (floating) 1f else 0f)
            shader.setColorUniform("tintColor", tint.toArgb())
        }
    }
}

private const val SceneProgram = """
uniform float2 sceneSize;
uniform float dark;
uniform float density;
uniform float diagnostic;
layout(color) uniform half4 accentColor;
float3 scene(float2 p) {
    if (diagnostic > 0.5) {
        float2 cell = abs(fract(p / (24.0*density)) - 0.5);
        float line = smoothstep(0.455, 0.49, max(cell.x, cell.y));
        return mix(float3(0.91, 0.95, 0.98), float3(0.13, 0.32, 0.48), line);
    }
    float2 uv = p / sceneSize;
    float3 col = mix(float3(0.945, 0.961, 0.975), float3(0.035, 0.052, 0.076), dark);
    float2 a = (uv-float2(0.02, 0.15))*float2(1.8, 2.0);
    float2 b = (uv-float2(0.96, 0.58))*float2(1.8, 2.4);
    float2 c = (uv-float2(0.08, 0.95))*float2(1.7, 2.4);
    col = mix(col, mix(float3(0.69, 0.86, 0.91), float3(0.06, 0.18, 0.22), dark), exp(-dot(a,a))*.48);
    col = mix(col, mix(float3(0.83, 0.79, 0.92), float3(0.15, 0.10, 0.23), dark), exp(-dot(b,b))*.42);
    col = mix(col, mix(float3(0.97, 0.85, 0.79), float3(0.19, 0.11, 0.13), dark), exp(-dot(c,c))*.36);
    float arc = uv.y - .77 + .15*sin(uv.x*3.8);
    float ribbon = exp(-(arc*arc)/.0036);
    col = mix(col, mix(float3(.68,.82,.88),float3(.08,.20,.25),dark), ribbon*.19);
    return mix(col, accentColor.rgb, .018);
}
"""

private const val BackgroundProgram = """
half4 main(float2 p) { return half4(scene(p), 1); }
"""

private const val LensProgram = """
uniform float2 panelSize;
uniform float2 origin;
uniform float radius;
uniform float press;
uniform float floating;
layout(color) uniform half4 tintColor;
half4 main(float2 p) {
    float2 c = p-panelSize*.5;
    float2 q = abs(c)-(panelSize*.5-radius);
    float d = length(max(q,0.0)) + min(max(q.x,q.y),0.0)-radius;
    float inset = max(-d,0.0);
    float2 corner = max(q,0.0);
    float2 outward = length(corner) > .001 ? normalize(corner)*sign(c)
        : (q.x > q.y ? float2(sign(c.x),0) : float2(0,sign(c.y)));

    // A quarter-ellipse joins the vertical rim to a flat face with zero slope.
    float bevel = min(11.0*density, min(panelSize.x,panelSize.y)*.40);
    float u = clamp(inset / max(bevel,.01), .008, 1.0);
    float h = sqrt(max(2.0*u-u*u,.0001));
    float slope = (8.0*density / max(bevel,.01))*(1.0-u)/h;
    float3 N = normalize(float3(outward*slope,1));
    float3 V = float3(0,0,1);

    // Air -> glass, n=1.46. The flat center has no displacement.
    float eta = 1.0/1.46;
    float k = 1.0-eta*eta*(1.0-N.z*N.z);
    float3 T = eta*float3(0,0,-1) + (eta*N.z-sqrt(max(k,0.0)))*N;
    float depth = (5.5+7.5*h)*density;
    float2 shift = T.xy/max(abs(T.z),.08)*depth;
    float grazing = 1.0-N.z;
    float F = .03497+.96503*grazing*grazing*grazing*grazing*grazing;

    // Two extended light sources create thin opposing glints, not a white border.
    float3 R = 2.0*dot(N,V)*N-V;
    float key = pow(max(dot(R,normalize(float3(-.55,-.85,.85))),0.0),28.0);
    float fill = pow(max(dot(R,normalize(float3(.85,.60,.65))),0.0),42.0);
    float3 ambient = mix(float3(.45,.50,.57),float3(.19,.23,.30),dark);
    float3 environment = ambient + key*float3(6.0,6.2,6.4) + fill*float3(2.5,2.6,2.8);
    float crease = exp(-inset/max(.45*density,.01))*(1.0-key)*.018;

    if (floating > .5) {
        // No cross-app sampling: premultiplied transmission + curved reflection.
        float a = mix(.46,.60,dark);
        a = clamp(a+(1.0-a)*F*.65,0.0,1.0);
        float3 face = toLinearSrgb(mix(float3(.98,.99,1),float3(.065,.080,.105),dark));
        face = mix(face,toLinearSrgb(tintColor.rgb),tintColor.a*.35);
        float3 lit = face*(1.0-F) + environment*F - crease;
        float3 rgb = fromLinearSrgb(clamp(lit,0.0,1.0));
        return half4(rgb*a,a);
    }

    float3 transmitted = toLinearSrgb(scene(origin+p+shift));
    transmitted *= exp(-float3(.0035,.0025,.0020)*depth/density/max(abs(T.z),.2));
    float3 regular = toLinearSrgb(mix(float3(1),float3(.055,.070,.095),dark));
    transmitted = mix(transmitted,regular,mix(.04,.07,dark));
    transmitted = mix(transmitted,toLinearSrgb(tintColor.rgb),tintColor.a*.4);
    float3 lit = transmitted*(1.0-F)+environment*F-crease;
    lit += press*.025;
    return half4(fromLinearSrgb(clamp(lit,0.0,1.0)),1);
}
"""
