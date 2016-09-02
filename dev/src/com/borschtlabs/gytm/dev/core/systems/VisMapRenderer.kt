package com.borschtlabs.gytm.dev.core.systems

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import com.borschtlabs.gytm.dev.core.Point

/**
 * @author octopussy
 */

class VisMapRenderer(fboWidth: Int = Gdx.graphics.width, fboHeight: Int = Gdx.graphics.height) : Disposable {

    private var frameBuffer: FrameBuffer

    internal val combined = Matrix4()
    internal val ambientLight = Color(0.3f, 0.3f, 0.3f, 1.0f)

    private val frameMesh: Mesh
    private val visMesh: Mesh = Mesh(false, 4096, 0, VertexAttribute(VertexAttributes.Usage.Position, 2, "a_position"))
    private var visMapVertices: FloatArray = FloatArray(16)

    internal var x1: Float = 0.toFloat()
    internal var x2: Float = 0.toFloat()
    internal var y1: Float = 0.toFloat()
    internal var y2: Float = 0.toFloat()

    private var shader: ShaderProgram
    private var frameShader: ShaderProgram

    init {
        frameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
        frameMesh = createFrameMesh()
        shader = createVisMapShader()
        frameShader = createFrameShader()
    }

    fun resizeFBO(fboWidth: Int, fboHeight: Int) {
        frameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, fboWidth, fboHeight, false);
    }

    fun createVisMapShader(): ShaderProgram {
        val gammaCorr = true
        var gamma = ""
        if (gammaCorr)
            gamma = "sqrt"

        val vertexShader = """
        attribute vec2 a_position;

        uniform mat4 u_projTrans;

        varying vec4 v_color;
        varying vec2 v_vertPos;

        void main()
        {
            v_color = vec4(1.0, 1.0, 1.0, 1.0);
            gl_Position =  u_projTrans * vec4(a_position.xy, 0.0, 1.0);

            v_vertPos = vec2(a_position.xy);

        }"""

        val fragmentShader = """

        #ifdef GL_ES
        precision mediump float;
        #endif

        varying vec4 v_color;
        varying vec2 v_vertPos;

        void main()
        {
            gl_FragColor = v_color;

        }"""

        ShaderProgram.pedantic = false
        val lightShader = ShaderProgram(vertexShader,
                fragmentShader)
        if (lightShader.isCompiled == false) {
            Gdx.app.log("ERROR", lightShader.log)
        }

        return lightShader
    }

    fun createFrameShader(): ShaderProgram {
        val vertexShader = """
        attribute vec4 a_position;
        attribute vec2 a_texCoord;

        varying vec2 v_texCoords;

        void main()
        {
           v_texCoords = a_texCoord;
           gl_Position = a_position;

        } """

        val fragmentShader = """
        #ifdef GL_ES

        precision lowp float;

        #define MED mediump
        #else
        #define MED
        #endif

        varying MED vec2 v_texCoords;

        uniform sampler2D u_texture;

        void main()
        {
            gl_FragColor = texture2D(u_texture, v_texCoords);
        } """

        ShaderProgram.pedantic = false
        val woShadowShader = ShaderProgram(vertexShader, fragmentShader)
        if (woShadowShader.isCompiled == false) {
            Gdx.app.log("ERROR", woShadowShader.log)

        }

        return woShadowShader
    }

    private fun createFrameMesh(): Mesh {
        val verts = FloatArray(VERT_SIZE)
        // vertex coord
        verts[X1] = -1f
        verts[Y1] = -1f

        verts[X2] = 1f
        verts[Y2] = -1f

        verts[X3] = 1f
        verts[Y3] = 1f

        verts[X4] = -1f
        verts[Y4] = 1f

        // tex coords
        verts[U1] = 0f
        verts[V1] = 0f

        verts[U2] = 1f
        verts[V2] = 0f

        verts[U3] = 1f
        verts[V3] = 1f

        verts[U4] = 0f
        verts[V4] = 1f

        val tmpMesh = Mesh(true, 4, 0, VertexAttribute(
                VertexAttributes.Usage.Position, 2, "a_position"), VertexAttribute(
                VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord"))

        tmpMesh.setVertices(verts)
        return tmpMesh

    }

    fun setCombinedMatrix(camera: OrthographicCamera) {
        this.setCombinedMatrix(
                camera.combined,
                camera.position.x,
                camera.position.y,
                camera.viewportWidth * camera.zoom,
                camera.viewportHeight * camera.zoom)
    }

    fun setCombinedMatrix(combined: Matrix4, x: Float, y: Float,
                          viewPortWidth: Float, viewPortHeight: Float) {

        System.arraycopy(combined.`val`, 0, this.combined.`val`, 0, 16)
        // updateCameraCorners
        val halfViewPortWidth = viewPortWidth * 0.5f
        x1 = x - halfViewPortWidth
        x2 = x + halfViewPortWidth

        val halfViewPortHeight = viewPortHeight * 0.5f
        y1 = y - halfViewPortHeight
        y2 = y + halfViewPortHeight
    }

    fun render(centers: List<Vector2>, entries: List<Array<Point>>) {

        frameBuffer.begin()
        Gdx.gl.glClearColor(ambientLight.r, ambientLight.g, ambientLight.b, 1.0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        Gdx.gl.glDepthMask(false)
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shader.begin()

        shader.setUniformMatrix("u_projTrans", combined)

        for (i in 0..centers.size - 1) {
            val center = centers[i]
            val e = entries[i]

            val newVerticesCount = e.size + 1
            if (visMapVertices.size < newVerticesCount * 2) {
                visMapVertices = FloatArray(newVerticesCount * 2)
            }

            visMapVertices[0] = center.x
            visMapVertices[1] = center.y

            var index = 1
            for (j in 1..e.size) {
                val next = e[j - 1]
                visMapVertices[index * 2] = next.position.x
                visMapVertices[index * 2 + 1] = next.position.y
                ++index
            }

            visMesh.setVertices(visMapVertices)
            visMesh.render(shader, GL20.GL_TRIANGLE_FAN, 0, e.size + 1)
        }

        shader.end()

        frameBuffer.end()

        renderFrameBuffer()
    }

    private fun renderFrameBuffer() {
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_ZERO, GL20.GL_SRC_COLOR)

        frameBuffer.colorBufferTexture.bind(0)
        frameShader.begin()

        frameMesh.render(frameShader, GL20.GL_TRIANGLE_FAN)

        frameShader.end()

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    override fun dispose() {

    }

    companion object {

        val VERT_SIZE = 16
        val X1 = 0
        val Y1 = 1
        val U1 = 2
        val V1 = 3
        val X2 = 4
        val Y2 = 5
        val U2 = 6
        val V2 = 7
        val X3 = 8
        val Y3 = 9
        val U3 = 10
        val V3 = 11
        val X4 = 12
        val Y4 = 13
        val U4 = 14
        val V4 = 15
    }
}