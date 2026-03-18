#!/usr/bin/env python3
"""Parse kover XML report and display branch coverage summary."""

import xml.etree.ElementTree as ET
import sys
import os

REPORT_PATH = os.path.join(
    os.path.dirname(__file__), "..", "dsl", "build", "reports", "kover", "report.xml"
)

def main():
    target = int(sys.argv[1]) if len(sys.argv) > 1 else 80

    if not os.path.exists(REPORT_PATH):
        print(f"Report not found at {REPORT_PATH}")
        print("Run: ./gradlew :dsl:koverXmlReport")
        sys.exit(1)

    tree = ET.parse(REPORT_PATH)
    root = tree.getroot()

    total_missed = 0
    total_covered = 0

    # Per-package
    pkg_data = []
    for pkg in root.findall(".//package"):
        pkg_name = pkg.get("name").replace("org/khorum/oss/konstellation/dsl/", "")
        for counter in pkg.findall("counter"):
            if counter.get("type") == "BRANCH":
                m = int(counter.get("missed", 0))
                c = int(counter.get("covered", 0))
                t = m + c
                total_missed += m
                total_covered += c
                if t > 0:
                    pkg_data.append((c / t * 100, c, t, m, pkg_name))

    grand_total = total_missed + total_covered
    pct = total_covered / grand_total * 100 if grand_total else 0
    needed = int(grand_total * target / 100)
    gap = max(0, needed - total_covered)

    status = "PASS" if pct >= target else "FAIL"
    print(f"\n{'='*60}")
    print(f"  BRANCH COVERAGE: {pct:.1f}% ({total_covered}/{grand_total})  [{status}]")
    print(f"  Target: {target}%  Need: {needed}  Gap: {gap}")
    print(f"{'='*60}\n")

    # Packages
    print("PACKAGES:")
    pkg_data.sort(key=lambda x: x[0])
    for p, c, t, m, name in pkg_data:
        bar = "█" * int(p / 5) + "░" * (20 - int(p / 5))
        marker = " ✓" if p >= target else " ✗"
        print(f"  {p:5.1f}% {bar} {name}{marker}")

    # Classes below target
    classes = []
    for pkg in root.findall(".//package"):
        for cls in pkg.findall("class"):
            cls_name = cls.get("name").split("/")[-1]
            for counter in cls.findall("counter"):
                if counter.get("type") == "BRANCH":
                    m = int(counter.get("missed", 0))
                    c = int(counter.get("covered", 0))
                    t = m + c
                    if t > 0 and m > 0:
                        classes.append((c / t * 100, m, c, t, cls_name))

    classes.sort()
    below = [x for x in classes if x[0] < target]
    above = [x for x in classes if x[0] >= target]

    if below:
        print(f"\nCLASSES BELOW {target}% ({len(below)}):")
        for p, m, c, t, name in below:
            print(f"  {p:5.1f}% ({c:3d}/{t:3d}) missed={m:3d}  {name}")

    if above:
        print(f"\nCLASSES AT/ABOVE {target}% ({len(above)}):")
        for p, m, c, t, name in above:
            print(f"  {p:5.1f}% ({c:3d}/{t:3d}) missed={m:3d}  {name}")

    print()
    return 0 if pct >= target else 1

if __name__ == "__main__":
    sys.exit(main())
