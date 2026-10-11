/**
 * DevLens Mock Dashboard Module
 */

export function mockGetStats() {
  return {
    totalAnalyses: 128,
    analysesGrowth: "+14% this week",
    bugsDetectedAndFixed: 42,
    highSeverityResolved: 8,
    avgQualityScore: 88,
    qualityTier: "Grade A Tier",
    activeProjects: 9,
    languages: ["Java", "Python", "C++", "TypeScript"]
  };
}

export function mockGetChartData() {
  return {
    activity: {
      labels: ["Sep 14", "Sep 18", "Sep 22", "Sep 26", "Sep 30", "Oct 04", "Oct 08", "Oct 11"],
      analysesCount: [12, 19, 14, 25, 22, 30, 28, 38],
      bugsFound: [3, 5, 2, 8, 4, 7, 5, 8]
    },
    distribution: {
      labels: ["Error Detection", "Complexity Analysis", "Code Review", "Flowchart Gen"],
      data: [45, 30, 15, 10]
    }
  };
}
