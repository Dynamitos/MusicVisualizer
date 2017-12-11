package data;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.io.File;

public class FileNameAdapter extends XmlAdapter<String, File>{

    @Override
    public File unmarshal(String v) throws Exception {
        return new File(v);
    }

    @Override
    public String marshal(File v) throws Exception {
        return v.getAbsolutePath();
    }
}
