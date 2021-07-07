import math
import bpy
import mathutils
import bmesh
from mathutils import Vector

class MakeTetrahedron(bpy.types.Operator) :
    bl_idname = "mesh.make_tetrahedron"
    bl_label = "Wall"
    bl_options = {"REGISTER", "UNDO"}     
    

    inverted = bpy.props.BoolProperty \
      (
        name = "Upside Down",
        description = "Generate the tetrahedron upside down",
        default = False
      )

    def draw(self, context) :
        TheCol = self.layout.column(align = True)
        TheCol.prop(self, "inverted")
    #end draw
    
    def generate_stripe(self, vectors, faces, cols, i, size, x,y,z):
        for j,w in enumerate(cols):
                vectors.append(Vector((x,y,z)))
                x += w
                if i > 0 and j > 0: 
                    faces.append([(i-1)*size + j - 1, (i-1)*size + j, i*size + j, i*size + j - 1])
            
        vectors.append(Vector((x,y,z)))
        if i > 0: 
            faces.append([(i-1)*size + size - 2, (i-1)*size + size - 1, i*size + size - 1, i*size + size - 2])
    #end generate_stripe

    def action_common(self, context) :
        Scale = -1 if self.inverted else 1
        # make this configurable
        gap = 3
        length = 50
        width = 30
        levels = 5
        wnd_length = 1.5
        level_height = 3
        cnt = int((length - gap * 2) / wnd_length)
        if cnt % 2 == 0:
            cnt -= 1
        real_gap = (1.0 * length - wnd_length * cnt) / 2;
        bottom_gap =  2.5
        top_gap =  0.5
        
        cols = []
        cols.append(real_gap)
        for i in range(cnt):
            cols.append(wnd_length)
        cols.append(real_gap)
        
        height_segs = []
        for i in range(levels):
            if i == 0:
                height_segs.append(bottom_gap)
            else:
                height_segs.append(level_height - wnd_length)
            height_segs.append(wnd_length)
            if i == levels - 1:
                height_segs.append(top_gap)
        
        vectors = []
        faces = []
        
        size = len(cols) + 1
        x = 0
        y = 0
        z = 0
        for i, seg_ht in enumerate(height_segs):
            self.generate_stripe(vectors, faces, cols, i, size, x, y, z)
            z += seg_ht      
            x =  0  
        self.generate_stripe(vectors, faces, cols, len(height_segs), size, x, y, z)
        NewMesh = bpy.data.meshes.new("Wall")
        NewMesh.from_pydata \
          (
            vectors,
            [],
            faces
          )
        NewMesh.update()
        NewObj = bpy.data.objects.new("Wall", NewMesh)        
        context.scene.objects.link(NewObj)
        bpy.ops.object.select_all(action = "DESELECT")
        NewObj.select = True
        context.scene.objects.active = NewObj
        bpy.ops.object.mode_set(mode='EDIT', toggle=False)
        
        cols_cnt = len(cols)
        bm = bmesh.from_edit_mesh(NewMesh)
        bm.faces.ensure_lookup_table()
        for face in bm.faces:
            face.select = False
        for i in range(cols_cnt + 1, 2*cols_cnt - 1, 2):
            bm.faces[i].select = True      
            
        # Now we have made a links of the chain, make a copy and rotate it
        # (so this looks something like a chain)

        ret = bmesh.ops.duplicate(
                bm,
                geom=bm.verts[:] + bm.edges[:] + bm.faces[:])
        geom_dupe = ret["geom"]
        verts_dupe = [ele for ele in geom_dupe if isinstance(ele, bmesh.types.BMVert)]
        del ret

        # position the new link
        bmesh.ops.translate(
                bm,
                verts=verts_dupe,
                vec=(0.0, 5.0, 0.0))
        bmesh.ops.rotate(
                bm,
                verts=verts_dupe,
                cent=(length / 2, 5.0, 0.0),
                matrix=mathutils.Matrix.Rotation(math.radians(180.0), 3, 'Z'))      

        # Show the updates in the viewport
        bmesh.update_edit_mesh(NewMesh, True)
        
        bpy.ops.mesh.extrude_faces_move(MESH_OT_extrude_faces_indiv={"mirror":False}, TRANSFORM_OT_shrink_fatten={"value":0.3, "use_even_offset":True, "mirror":False, "proportional":'DISABLED', "proportional_edit_falloff":'SMOOTH', "proportional_size":1, "snap":False, "snap_target":'CLOSEST', "snap_point":(0, 0, 0), "snap_align":False, "snap_normal":(0, 0, 0), "release_confirm":False})
        NewMesh.update()
        # Show the updates in the viewport
        bmesh.update_edit_mesh(NewMesh, True)        
    #end action_common 
   
    def execute(self, context) :
        self.action_common(context)
        return {"FINISHED"}
    #end execute

    def invoke(self, context, event) :
        self.action_common(context)
        return {"FINISHED"}
    #end invoke

#end MakeTetrahedron

def add_to_menu(self, context) :
    self.layout.operator("mesh.make_tetrahedron", icon = "PLUGIN")
#end add_to_menu

def register() :
    bpy.utils.register_class(MakeTetrahedron)
    bpy.types.INFO_MT_mesh_add.append(add_to_menu)
#end register

def unregister() :
    bpy.utils.unregister_class(MakeTetrahedron)
    bpy.types.INFO_MT_mesh_add.remove(add_to_menu)
#end unregister

if __name__ == "__main__" :
    register()
#end if