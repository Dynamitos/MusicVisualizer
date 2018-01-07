package image;

import data.Profile;

public class RenderModeFactory {
    private RenderModeFactory(){

    }
    public static RenderMode createRenderMode(Profile p)
    {
        RenderMode result;
        if(p.isRecording())
        {
            result = new RecordingRenderer();
        }
        else
        {
            result = new MasterRenderer();
        }
        result.init(p);
        return result;
    }
}
