#!/bin/bash
sleep 2
systemctl --user restart pipewire pipewire-pulse wireplumber
pactl set-sink-volume @DEFAULT_SINK@ 50%
