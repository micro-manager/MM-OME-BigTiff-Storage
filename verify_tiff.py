#!/usr/bin/env python
"""Cross-language conformance check for MM-OME-BigTiff-Storage.

Reads target/verify-fixture.ome.tiff (written by VerificationFixtureTest) with the independent
`tifffile` stack and asserts that the file is a valid pyramidal OME-BigTIFF: BigTIFF header,
OME-XML with the right dimensions/channels, a SubIFD pyramid with the expected level sizes, and
exact full-resolution pixel values.

Requires: tifffile, numpy   (pip install tifffile numpy)
"""
import sys
from pathlib import Path

import numpy as np
import tifffile

W, H = 96, 64
SIZE_T, SIZE_C, SIZE_Z = 2, 2, 3
LEVELS = 3


def expected_plane(t, c, z):
    y, x = np.mgrid[0:H, 0:W]
    return ((x + y + 100 * t + 10 * c + z) & 0xFFFF).astype(np.uint16)


def main():
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "target/verify-fixture.ome.tiff")
    tif_path = root / "verify-fixture.ome.tif"
    assert tif_path.exists(), f"fixture not found: {tif_path} (run `mvn test` first)"

    fails = []

    def check(cond, msg):
        (print("  ok:", msg) if cond else fails.append(msg))
        if not cond:
            print("  FAIL:", msg)

    with tifffile.TiffFile(str(tif_path)) as tif:
        check(tif.is_bigtiff, "file is BigTIFF (version 43, 8-byte offsets)")
        check(tif.is_ome, "file carries OME-XML metadata")

        ome = tif.ome_metadata or ""
        check('DimensionOrder="XYZCT"' in ome, "OME DimensionOrder is XYZCT")
        check(f'SizeX="{W}"' in ome and f'SizeY="{H}"' in ome, "OME SizeX/SizeY correct")
        check(f'SizeZ="{SIZE_Z}"' in ome and f'SizeC="{SIZE_C}"' in ome
              and f'SizeT="{SIZE_T}"' in ome, "OME SizeZ/SizeC/SizeT correct")
        check('Name="DAPI"' in ome and 'Name="FITC"' in ome, "OME channel names present")

        series = tif.series[0]
        check(series.shape == (SIZE_T, SIZE_C, SIZE_Z, H, W),
              f"series shape {series.shape} == (T,C,Z,Y,X)=({SIZE_T},{SIZE_C},{SIZE_Z},{H},{W})")

        # Pyramid: tifffile exposes reduced-resolution SubIFDs as series levels.
        levels = series.levels
        check(len(levels) == LEVELS, f"pyramid has {LEVELS} levels (got {len(levels)})")
        exp_hw = [(H, W)]
        for _ in range(1, LEVELS):
            ph, pw = exp_hw[-1]
            exp_hw.append(((ph + 1) // 2, (pw + 1) // 2))
        for lvl, (eh, ew) in enumerate(exp_hw):
            ls = levels[lvl].shape
            check(ls[-2:] == (eh, ew), f"level {lvl} spatial size {ls[-2:]} == {(eh, ew)}")

        # Full-resolution pixels match exactly.
        data = series.asarray()  # (T, C, Z, Y, X)
        all_match = True
        for t in range(SIZE_T):
            for c in range(SIZE_C):
                for z in range(SIZE_Z):
                    if not np.array_equal(data[t, c, z], expected_plane(t, c, z)):
                        all_match = False
        check(all_match, "all full-resolution planes match expected pixel values")

        # Level-1 of plane (0,0,0) equals a 2x2 block-average of the base plane.
        base = expected_plane(0, 0, 0).astype(np.float64)
        dh, dw = (H + 1) // 2, (W + 1) // 2
        ref = np.zeros((dh, dw), np.uint16)
        for yy in range(dh):
            for xx in range(dw):
                block = base[2 * yy:2 * yy + 2, 2 * xx:2 * xx + 2]
                ref[yy, xx] = int(block.mean())
        lvl1 = levels[1].asarray()
        lvl1_000 = lvl1[0, 0, 0] if lvl1.ndim == 5 else lvl1
        check(np.array_equal(lvl1_000, ref),
              "level-1 downsample of plane (0,0,0) matches 2x2 averaging")

    if fails:
        print(f"\n{len(fails)} check(s) FAILED")
        sys.exit(1)
    print("\nAll checks passed: valid pyramidal OME-BigTIFF.")


if __name__ == "__main__":
    main()
