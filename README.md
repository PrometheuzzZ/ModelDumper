# ModelDumper

Adds `dump` hotkeys(not bound by default!) to the game that dumps the currently rendered models to a file.
The output file is a `.obj` file that can be opened in any 3D modeling software and the required textures are also dumped. The model is uv mapped, so the textures should be applied correctly. Important: The model is not rigged, it is just a static model in the current pose of the game.
In addition, a combined `.glb` model is generated with textures configured for pixel art (NEAREST filtering and clamped edges) and full alpha channel support.
Currently works with:

- Players
- All entities
- Only display entities

Everything connected to the player or entities like armor, held items, capes, 3d skin layers, etc. is also dumped.
Files will be located in the `ModelDumps` folder in the game directory.

## Dependencies

- FabricAPI

## License

## Original source code and mod
[https://github.com/tr7zw/ModelDumper](https://github.com/tr7zw/ModelDumper)

