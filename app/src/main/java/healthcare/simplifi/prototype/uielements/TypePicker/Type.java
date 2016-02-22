package healthcare.simplifi.prototype.uielements.TypePicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Random;

import healthcare.simplifi.prototype.R;

/**
 * Created by Viviano on 5/19/2015.
 */
public class Type {
    public int color;
    public Bitmap icon;
    public SVG svgIcon;
    public String name, searchAddon;

    public Type(String name, String searchAddon, Bitmap icon, SVG svgIcon, int color) {
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.svgIcon = svgIcon;
        this.searchAddon = searchAddon;
    }

    @Override
    public String toString() {
        return searchAddon;
    }

    public static class Tree {
        public LinkedList<Tree> leaves = new LinkedList<>();
        public Tree parent = null;
        public Type data;

        public Tree(Tree parent, Type data) {
            this.parent = parent;
            this.data = data;
        }

        public int getHeight(){
            if (parent == null)
                return 1;
            return 1 + parent.getHeight();
        }

        public int getDepth() {
            if (!hasChild())
                return 1;
            int[] heights = new int[leaves.size()];
            for (int i=0; i<leaves.size(); i++) {
                heights[i] = get(i).getDepth();
            }
            return 1 + greatest(heights);
        }
        private int greatest(int[] list) {
            int curr = list[0];
            for (int i=1; i<list.length; i++) {
                if (list[i] > curr)
                    curr = list[i];
            }
            return curr;
        }

        public int size() {
            return leaves.size();
        }

        public void add(Type data) {
            leaves.add(new Tree(this, data));
        }

        public Tree get(int index) {
            return leaves.get(index);
        }
        public Tree get(int[] path) {
            if (path[0] == -1)
                return this;

            int[] rest = new int[path.length-1];
            for (int i=0; i<rest.length; i++) {
                rest[i] = path[i+1];
            }
            return get(path[0]).get(rest);
        }

        public boolean hasChild() {
            return size() > 0;
        }

        public int getIndex() {
            if (parent == null)
                return 0;
            for (int i=0; i<parent.leaves.size(); i++) {
                if (parent.get(i).equals(this))
                    return i;
            }
            return -1;
        }
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Tree))
                return false;
            return data.searchAddon.equalsIgnoreCase(((Tree) o).data.searchAddon);
        }
        @Override
        public String toString() {
            return data.toString();
        }
    }

    //Type Tree
    public static Tree TYPE_TREE;

    public static void loadTypes(Context context) {
        TYPE_TREE = new Tree(null, new Type("main", "no_search", null, null, 0x0FFB20000));

        Resources res = context.getResources();
        try {
            InputStream inputStream = res.openRawResource(R.raw.types);
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            JSONObject jObject = new JSONObject(new String(b));
            loadTypesFromJSON(TYPE_TREE, jObject, context);
        } catch (JSONException e) {
            Log.e("Exception", e.toString());
        } catch (IOException e){
            Log.e("Exception", e.toString());
        }
    }

    private static void loadTypesFromJSON(Tree tree, JSONObject jObject, Context context) {
        try {
            Random r = new Random();
            if (jObject.has("types")) {
                JSONArray arr = jObject.getJSONArray("types");
                for (int i=0; i<arr.length(); i++) {
                    JSONObject curr = arr.getJSONObject(i);
                    //add color if available
                    int color = COLORS[(int) r.nextInt(COLORS.length)];
                    if (curr.has("color"))
                        color = Integer.parseInt(curr.getString("color"), 16)+0xFF000000;
                    //add search addon if available
                    String search = "no_search";
                    if (curr.has("search"))
                        search = curr.getString("search");
                    //add icon if it exists
                    Bitmap ic = null;
                    SVG svg = null;
                    if (curr.has("icon")) {
                        try {
                            String s = curr.getString("icon");
                            Resources res = context.getResources();
                            if (!s.contains("flag"))
                                ic = BitmapFactory.decodeResource(res,
                                        res.getIdentifier(s,"drawable", context.getPackageName()));
                            else {
                                System.out.println(s);
                                svg = new SVGBuilder().readFromResource(res,
                                        res.getIdentifier(s, "raw", context.getPackageName())).build();
                            }
                        } catch (Exception e){}
                    }
                    //Add to tree and recurr
                    tree.add(new Type(curr.getString("name"), search, ic, svg, color));
                    loadTypesFromJSON(tree.get(i), curr, context);
                }
            }
        } catch (JSONException e) {
            Log.e("Exception", e.toString());
        }
    }

    private static int[] COLORS = new int[] { 0xFFe03661, 0xFFbc36e0, 0xFF3639e0, 0xFF36b9e0,
            0xFFe0d946, 0xFFe0ae36, 0xFF36e041, 0xFFe03636, 0xFF1f552a, 0xFFf06e01,
            0xFF5ae32f, 0xFF5236ce, 0xFF2f6263 };
}
