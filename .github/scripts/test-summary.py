"""Writes a Markdown summary of the Gradle test results (main run and rerun) for $GITHUB_STEP_SUMMARY.

Usage: python3 -I .github/scripts/test-summary.py <title> >> "$GITHUB_STEP_SUMMARY"
"""
import glob
import re
import sys
import xml.etree.ElementTree as ET

RUNS = [("Run", "build/test-results/test"), ("Rerun of failures", "build/test-results/rerunFailed")]
# cucumber-testng names each test runScenario[n]("<scenario>", "<feature>")
SCENARIO_NAME = re.compile(r'^runScenario\[\d+\]\("(.*)", "(.*)"\)$')


def scenario(testcase):
    match = SCENARIO_NAME.match(testcase.get("name", ""))
    return f"{match[2]}: {match[1]}" if match else testcase.get("name", "?")


def main():
    title = sys.argv[1] if len(sys.argv) > 1 else "E2E tests"
    lines = [f"### {title}", "", "| | Passed | Failed | Skipped | Time |", "|---|---:|---:|---:|---:|"]
    failed = {}
    for label, directory in RUNS:
        files = glob.glob(f"{directory}/*.xml")
        if not files:
            continue
        passed = failures = skipped = 0
        seconds = 0.0
        failed[label] = []
        for file in files:
            suite = ET.parse(file).getroot()
            seconds += float(suite.get("time", 0))  # wall clock; scenarios run in parallel
            for case in suite.iter("testcase"):
                if case.find("failure") is not None or case.find("error") is not None:
                    failures += 1
                    failed[label].append(scenario(case))
                elif case.find("skipped") is not None:
                    skipped += 1
                else:
                    passed += 1
        lines.append(f"| {label} | {passed} | {failures} | {skipped} | {seconds:.0f}s |")
    if not failed:
        lines.append("| No test results | | | | |")
    for label, names in failed.items():
        if names:
            lines += ["", f"**Failed in {label.lower()}:**", ""] + [f"- {name}" for name in sorted(names)]
    print("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
