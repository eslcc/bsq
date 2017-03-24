import React, {Component, PropTypes} from 'react';
import './Teams.css';
import Carousel from 'nuka-carousel';

export default class Teams extends Component {
    static propTypes = {
        teams: PropTypes.arrayOf(PropTypes.shape({
            number: PropTypes.string,
            members: PropTypes.arrayOf(PropTypes.string)
        })).isRequired,
    };

    static mapToMultiDimen(teams) {
        const result = [];
        teams.forEach((team, index) => {
            const mod = index % 4;
            if (mod === 0) {
                result.push([]);
            }
            result[result.length - 1].push(team);
        });
        return result;
    }

    render() {
        if (!this.props.teams || this.props.teams.length === 0) {
            return (
                <div>Loading teams</div>
            );
        }
        const data = Teams.mapToMultiDimen(this.props.teams);
        const sliderSettings = {
            autoplay: true,
            autoplayInterval: 7000,
            dragging: false,
            swiping: false,
            decorators: [],
            wrapAround: true,
        };
        return (
            <Carousel {...sliderSettings}>
                {data.map((list, index) => (
                    <div key={`teams_${index}`} className="team-container">
                        {list.map((team, teamIndex) => (
                            <div key={team.number} className={`team team-${(teamIndex % 4) + 1}`}>
                                <h3 className="team-number">{team.number}</h3>
                                <ul>
                                    {team.memberNames.map(name => (
                                        <li key={name}>{name}</li>
                                    ))}
                                </ul>
                            </div>
                        ))}
                    </div>
                ))}
            </Carousel>
        );
    }
}
