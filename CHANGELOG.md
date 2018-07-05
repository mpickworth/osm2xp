# Changelog
All notable changes to OSM 2 XP project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [3.1.0]

### Added

- Basic multipolygon support (works for forest, but not for buildings yet - buildings still don't have "holes")

### Fixed
- Polygon simplification logics - it still gave bad results in some cases before 
- Some UI issues

### Changed 
- Removed some old and unused libraries from build

## [3.0.1]

### Added

- Ability to generate bridges for roads and railways

### Fixed

- A critical issue with wrong decimal format - this led to using "," instead of "." as decimal sign on some systems and generating invalid scenario in such case  

## [3.0.0]

### Added

- Generate roads with guessing lane count from tags
- Generate railways 
- Generate power lines
- Generate chimneys using object selection by chimney height
- Generate tanks/gasometers using special facade
- Generate barriers, two types supported - fence and walls
- Facade editor - added facade preview, ability to specify fence/wall facade and some small features
- Default facade set is shipped with program archive
- Generate debug images - 2048*2048 png file can be created for each tile with generated buildings, roads etc. on it with scale 1px=1m
- Ability to generate exclusion zone based on actual OSM file coverage, do not exlude whole tile 
- Ability to show console view, since it can be useful for diagnosing map generation problems and bugs 

### Changed
- Use _building:levels_ tag for getting building height when there's no _height_ tag specified
- Do not generate buildings for polygons with most of _man___made_ constants specified - such objects usually aren't a regular buildings
- Use special facade for building with type _garage_ - regular facades for garages give poor results

### Fixed

- Incorrect distance computation when simplifying polygons - if "simplify shapes" was chosen, many buldings became octagons and looked incorrect.
- Coordinates confusion - y is now latitude, x - longtitude, like it's on usual map  


## [2.0.0]

Original version by Benjamin Blanchet with ability to generate buildings and forest zones by OSM data
 